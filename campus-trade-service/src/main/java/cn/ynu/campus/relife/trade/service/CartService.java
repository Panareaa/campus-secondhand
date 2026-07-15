package cn.ynu.campus.relife.trade.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.trade.client.ItemFeignClient;
import cn.ynu.campus.relife.trade.client.StockFeignClient;
import cn.ynu.campus.relife.trade.client.dto.FeignItemSaleCheckVO;
import cn.ynu.campus.relife.trade.client.dto.FeignStockAvailableVO;
import cn.ynu.campus.relife.trade.domain.CartEntry;
import cn.ynu.campus.relife.trade.dto.AddCartItemRequest;
import cn.ynu.campus.relife.trade.dto.AddCartItemVO;
import cn.ynu.campus.relife.trade.dto.CartItemVO;
import cn.ynu.campus.relife.trade.dto.CartVO;
import cn.ynu.campus.relife.trade.dto.UpdateCartItemRequest;
import cn.ynu.campus.relife.trade.mapper.CartEntryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartEntryMapper cartEntryMapper;
    private final ItemFeignClient itemFeignClient;
    private final StockFeignClient stockFeignClient;

    public CartService(CartEntryMapper cartEntryMapper,
                       ItemFeignClient itemFeignClient,
                       StockFeignClient stockFeignClient) {
        this.cartEntryMapper = cartEntryMapper;
        this.itemFeignClient = itemFeignClient;
        this.stockFeignClient = stockFeignClient;
    }

    public CartVO list(Long buyerId) {
        List<CartEntry> entries = cartEntryMapper.selectList(new LambdaQueryWrapper<CartEntry>()
                .eq(CartEntry::getBuyerId, buyerId)
                .orderByDesc(CartEntry::getUpdatedAt));
        List<CartItemVO> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        for (CartEntry entry : entries) {
            CartItemVO vo = toVO(entry);
            items.add(vo);
            if (Boolean.TRUE.equals(vo.getValid())) {
                totalAmount = totalAmount.add(vo.getLineAmount());
                totalQuantity += vo.getQuantity();
            }
        }
        CartVO cart = new CartVO();
        cart.setItems(items);
        cart.setTotalAmount(totalAmount);
        cart.setTotalQuantity(totalQuantity);
        return cart;
    }

    @Transactional
    public AddCartItemVO add(Long buyerId, AddCartItemRequest request) {
        FeignItemSaleCheckVO item = requireOnSaleItem(request.getItemId());
        if (buyerId.equals(item.getSellerId())) {
            throw new BusinessException(ErrorCode.CANNOT_BUY_OWN_ITEM);
        }
        requireStock(request.getItemId(), request.getQuantity());

        CartEntry existing = cartEntryMapper.selectOne(new LambdaQueryWrapper<CartEntry>()
                .eq(CartEntry::getBuyerId, buyerId)
                .eq(CartEntry::getItemId, request.getItemId()));
        if (existing != null) {
            int newQty = existing.getQuantity() + request.getQuantity();
            requireStock(request.getItemId(), newQty);
            existing.setQuantity(newQty);
            existing.setItemTitle(item.getTitle());
            existing.setItemPrice(item.getSalePrice());
            existing.setItemCover(item.getCoverUrl() != null ? item.getCoverUrl() : "");
            cartEntryMapper.updateById(existing);
            return new AddCartItemVO(existing.getId(), existing.getItemId(), existing.getQuantity());
        }

        CartEntry entry = new CartEntry();
        entry.setBuyerId(buyerId);
        entry.setItemId(request.getItemId());
        entry.setQuantity(request.getQuantity());
        entry.setItemTitle(item.getTitle());
        entry.setItemPrice(item.getSalePrice());
        entry.setItemCover(item.getCoverUrl() != null ? item.getCoverUrl() : "");
        cartEntryMapper.insert(entry);
        return new AddCartItemVO(entry.getId(), entry.getItemId(), entry.getQuantity());
    }

    @Transactional
    public AddCartItemVO updateQuantity(Long buyerId, Long itemId, UpdateCartItemRequest request) {
        CartEntry entry = requireEntry(buyerId, itemId);
        requireOnSaleItem(itemId);
        requireStock(itemId, request.getQuantity());
        entry.setQuantity(request.getQuantity());
        cartEntryMapper.updateById(entry);
        return new AddCartItemVO(entry.getId(), entry.getItemId(), entry.getQuantity());
    }

    @Transactional
    public void remove(Long buyerId, Long itemId) {
        int deleted = cartEntryMapper.delete(new LambdaQueryWrapper<CartEntry>()
                .eq(CartEntry::getBuyerId, buyerId)
                .eq(CartEntry::getItemId, itemId));
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
    }

    @Transactional
    public void clear(Long buyerId) {
        cartEntryMapper.delete(new LambdaQueryWrapper<CartEntry>()
                .eq(CartEntry::getBuyerId, buyerId));
    }

    private CartEntry requireEntry(Long buyerId, Long itemId) {
        CartEntry entry = cartEntryMapper.selectOne(new LambdaQueryWrapper<CartEntry>()
                .eq(CartEntry::getBuyerId, buyerId)
                .eq(CartEntry::getItemId, itemId));
        if (entry == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return entry;
    }

    private FeignItemSaleCheckVO requireOnSaleItem(Long itemId) {
        ApiResponse<FeignItemSaleCheckVO> response = itemFeignClient.saleCheck(itemId);
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        FeignItemSaleCheckVO item = response.getData();
        if (!Boolean.TRUE.equals(item.getOnSale())) {
            throw new BusinessException(ErrorCode.ITEM_NOT_ON_SALE);
        }
        return item;
    }

    private void requireStock(Long itemId, int quantity) {
        ApiResponse<FeignStockAvailableVO> response = stockFeignClient.available(itemId);
        if (response == null) {
            throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
        }
        // Stock Feign Fallback returns 50001 when stock-service is down
        if (response.getCode() == ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode()) {
            throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
        }
        if (response.getCode() != 0 || response.getData() == null) {
            throw new BusinessException(ErrorCode.STOCK_RECORD_NOT_FOUND);
        }
        Integer available = response.getData().getAvailableQty();
        if (available == null || available < quantity) {
            throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
        }
    }

    private CartItemVO toVO(CartEntry entry) {
        CartItemVO vo = new CartItemVO();
        vo.setCartEntryId(entry.getId());
        vo.setItemId(entry.getItemId());
        vo.setItemTitle(entry.getItemTitle());
        vo.setItemCover(entry.getItemCover());
        vo.setItemPrice(entry.getItemPrice());
        vo.setQuantity(entry.getQuantity());
        vo.setLineAmount(entry.getItemPrice().multiply(BigDecimal.valueOf(entry.getQuantity())));
        try {
            FeignItemSaleCheckVO item = requireOnSaleItem(entry.getItemId());
            requireStock(entry.getItemId(), entry.getQuantity());
            vo.setValid(true);
            vo.setItemTitle(item.getTitle());
            vo.setItemPrice(item.getSalePrice());
            vo.setLineAmount(item.getSalePrice().multiply(BigDecimal.valueOf(entry.getQuantity())));
        } catch (BusinessException ex) {
            // Downstream outage must fail checkout with 50001, not look like an empty cart
            if (ex.getCode() == ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode()) {
                throw ex;
            }
            vo.setValid(false);
            vo.setInvalidReason(ex.getMessage());
        }
        return vo;
    }
}
