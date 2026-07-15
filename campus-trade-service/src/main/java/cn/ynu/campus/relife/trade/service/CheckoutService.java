package cn.ynu.campus.relife.trade.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.trade.client.ItemFeignClient;
import cn.ynu.campus.relife.trade.client.StockFeignClient;
import cn.ynu.campus.relife.trade.client.UserFeignClient;
import cn.ynu.campus.relife.trade.client.dto.FeignBatchSaleCheckRequest;
import cn.ynu.campus.relife.trade.client.dto.FeignItemSaleCheckVO;
import cn.ynu.campus.relife.trade.client.dto.FeignLockStockRequest;
import cn.ynu.campus.relife.trade.client.dto.FeignStockAvailableVO;
import cn.ynu.campus.relife.trade.client.dto.FeignStockLockResultVO;
import cn.ynu.campus.relife.trade.client.dto.FeignUserValidateVO;
import cn.ynu.campus.relife.trade.config.TradeProperties;
import cn.ynu.campus.relife.trade.domain.TradeLine;
import cn.ynu.campus.relife.trade.domain.TradeOrder;
import cn.ynu.campus.relife.trade.dto.CartItemVO;
import cn.ynu.campus.relife.trade.dto.CartVO;
import cn.ynu.campus.relife.trade.dto.CheckoutLineVO;
import cn.ynu.campus.relife.trade.dto.CheckoutRequest;
import cn.ynu.campus.relife.trade.dto.CheckoutVO;
import cn.ynu.campus.relife.trade.dto.SettlePreviewVO;
import cn.ynu.campus.relife.trade.mapper.TradeLineMapper;
import cn.ynu.campus.relife.trade.mapper.TradeOrderMapper;
import cn.ynu.campus.relife.trade.support.TradeOrderRepository;
import cn.ynu.campus.relife.trade.support.TradeStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CheckoutService {

    private static final int STATUS_PENDING = TradeStatus.PENDING;

    private final CartService cartService;
    private final UserFeignClient userFeignClient;
    private final ItemFeignClient itemFeignClient;
    private final StockFeignClient stockFeignClient;
    private final TradeOrderMapper tradeOrderMapper;
    private final TradeLineMapper tradeLineMapper;
    private final IdempotencyService idempotencyService;
    private final TradeOrderService tradeOrderService;
    private final TradeProperties tradeProperties;

    public CheckoutService(CartService cartService,
                           UserFeignClient userFeignClient,
                           ItemFeignClient itemFeignClient,
                           StockFeignClient stockFeignClient,
                           TradeOrderMapper tradeOrderMapper,
                           TradeLineMapper tradeLineMapper,
                           IdempotencyService idempotencyService,
                           TradeOrderService tradeOrderService,
                           TradeProperties tradeProperties) {
        this.cartService = cartService;
        this.userFeignClient = userFeignClient;
        this.itemFeignClient = itemFeignClient;
        this.stockFeignClient = stockFeignClient;
        this.tradeOrderMapper = tradeOrderMapper;
        this.tradeLineMapper = tradeLineMapper;
        this.idempotencyService = idempotencyService;
        this.tradeOrderService = tradeOrderService;
        this.tradeProperties = tradeProperties;
    }

    public SettlePreviewVO settlePreview(Long buyerId) {
        FeignUserValidateVO user = requireValidUser(buyerId);
        List<CartItemVO> validItems = requireValidCartItems(buyerId);
        SettlePreviewVO vo = new SettlePreviewVO();
        vo.setItems(validItems);
        vo.setTotalAmount(sumAmount(validItems));
        vo.setContactInfo(user.getContactInfo());
        vo.setContactReady(user.getContactReady());
        return vo;
    }

    @Transactional
    public CheckoutVO checkout(Long buyerId, CheckoutRequest request, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID);
        }
        CheckoutVO cached = idempotencyService.getCached(buyerId, idempotencyKey);
        if (cached != null) {
            return cached;
        }

        FeignUserValidateVO user = requireValidUser(buyerId);
        if (!Boolean.TRUE.equals(user.getContactReady())) {
            throw new BusinessException(ErrorCode.CONTACT_REQUIRED);
        }

        List<CartItemVO> validItems = requireValidCartItems(buyerId);
        Map<Long, FeignItemSaleCheckVO> itemMap = batchSaleCheck(validItems);
        Long sellerId = requireSingleSeller(validItems, itemMap, buyerId);
        validateStock(validItems);

        String tradeNo = generateTradeNo();
        BigDecimal totalAmount = sumAmount(validItems);

        TradeOrder order = new TradeOrder();
        order.setTradeNo(tradeNo);
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setTotalAmount(totalAmount);
        order.setStatus(STATUS_PENDING);
        order.setBuyerContact(user.getContactInfo());
        order.setExpiredAt(LocalDateTime.now().plusMinutes(tradeProperties.getPendingTimeoutMinutes()));
        tradeOrderMapper.insert(order);

        List<CheckoutLineVO> lines = new ArrayList<>();
        for (CartItemVO cartItem : validItems) {
            FeignItemSaleCheckVO item = itemMap.get(cartItem.getItemId());
            BigDecimal lineAmount = item.getSalePrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            TradeLine line = new TradeLine();
            line.setTradeId(order.getId());
            line.setTradeNo(tradeNo);
            line.setBuyerId(buyerId);
            line.setItemId(cartItem.getItemId());
            line.setSellerId(item.getSellerId());
            line.setItemTitle(item.getTitle());
            line.setItemCover(cartItem.getItemCover());
            line.setUnitPrice(item.getSalePrice());
            line.setQuantity(cartItem.getQuantity());
            line.setLineAmount(lineAmount);
            tradeLineMapper.insert(line);
            lines.add(new CheckoutLineVO(cartItem.getItemId(), item.getTitle(),
                    item.getSalePrice(), cartItem.getQuantity(), lineAmount));

            lockStock(cartItem.getItemId(), order.getId(), tradeNo, cartItem.getQuantity());
        }

        cartService.clear(buyerId);

        CheckoutVO result = new CheckoutVO();
        result.setTradeNo(tradeNo);
        result.setTradeId(order.getId());
        result.setTotalAmount(totalAmount);
        result.setStatus(STATUS_PENDING);
        result.setStatusText(TradeStatus.text(STATUS_PENDING));
        result.setLines(lines);
        result.setCreatedAt(LocalDateTime.now());
        idempotencyService.cache(buyerId, idempotencyKey, result);
        String notifyTitle = itemMap.get(validItems.get(0).getItemId()).getTitle();
        tradeOrderService.notifyOrderCreated(order, notifyTitle);
        return result;
    }

    private FeignUserValidateVO requireValidUser(Long buyerId) {
        ApiResponse<FeignUserValidateVO> response = userFeignClient.validate(buyerId);
        if (response == null || response.getCode() != 0 || response.getData() == null
                || !Boolean.TRUE.equals(response.getData().getValid())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return response.getData();
    }

    private List<CartItemVO> requireValidCartItems(Long buyerId) {
        CartVO cart = cartService.list(buyerId);
        List<CartItemVO> validItems = cart.getItems().stream()
                .filter(item -> Boolean.TRUE.equals(item.getValid()))
                .toList();
        if (validItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }
        return validItems;
    }

    private Map<Long, FeignItemSaleCheckVO> batchSaleCheck(List<CartItemVO> items) {
        List<Long> itemIds = items.stream().map(CartItemVO::getItemId).toList();
        ApiResponse<List<FeignItemSaleCheckVO>> response =
                itemFeignClient.batchSaleCheck(new FeignBatchSaleCheckRequest(itemIds));
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
        }
        Map<Long, FeignItemSaleCheckVO> map = new HashMap<>();
        for (FeignItemSaleCheckVO item : response.getData()) {
            if (!Boolean.TRUE.equals(item.getOnSale())) {
                throw new BusinessException(ErrorCode.ITEM_NOT_ON_SALE);
            }
            map.put(item.getItemId(), item);
        }
        return map;
    }

    private Long requireSingleSeller(List<CartItemVO> items, Map<Long, FeignItemSaleCheckVO> itemMap, Long buyerId) {
        Set<Long> sellers = new HashSet<>();
        for (CartItemVO cartItem : items) {
            FeignItemSaleCheckVO item = itemMap.get(cartItem.getItemId());
            if (item == null) {
                throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
            }
            if (buyerId.equals(item.getSellerId())) {
                throw new BusinessException(ErrorCode.CANNOT_BUY_OWN_ITEM);
            }
            sellers.add(item.getSellerId());
        }
        if (sellers.size() > 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID);
        }
        return sellers.iterator().next();
    }

    private void validateStock(List<CartItemVO> items) {
        for (CartItemVO item : items) {
            ApiResponse<FeignStockAvailableVO> response = stockFeignClient.available(item.getItemId());
            if (response == null
                    || response.getCode() == ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode()) {
                throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
            }
            if (response.getCode() != 0 || response.getData() == null) {
                throw new BusinessException(ErrorCode.STOCK_RECORD_NOT_FOUND);
            }
            Integer available = response.getData().getAvailableQty();
            if (available == null || available < item.getQuantity()) {
                throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
            }
        }
    }

    private void lockStock(Long itemId, Long tradeId, String tradeNo, int quantity) {
        ApiResponse<FeignStockLockResultVO> response = stockFeignClient.lock(
                new FeignLockStockRequest(itemId, tradeId, tradeNo, quantity));
        if (response == null
                || response.getCode() == ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode()) {
            throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
        }
        if (response.getCode() != 0 || response.getData() == null
                || !Boolean.TRUE.equals(response.getData().getSuccess())) {
            throw new BusinessException(ErrorCode.STOCK_LOCK_FAILED);
        }
    }

    private BigDecimal sumAmount(List<CartItemVO> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItemVO item : items) {
            total = total.add(item.getLineAmount());
        }
        return total;
    }

    private String generateTradeNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "T" + time + random;
    }
}
