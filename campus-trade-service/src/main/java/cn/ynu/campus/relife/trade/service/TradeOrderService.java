package cn.ynu.campus.relife.trade.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.common.core.result.PageResult;
import cn.ynu.campus.relife.trade.client.StockFeignClient;
import cn.ynu.campus.relife.trade.client.UserFeignClient;
import cn.ynu.campus.relife.trade.client.dto.FeignPointGrantRequest;
import cn.ynu.campus.relife.trade.client.dto.FeignPointGrantResultVO;
import cn.ynu.campus.relife.trade.client.dto.FeignReleaseStockRequest;
import cn.ynu.campus.relife.trade.domain.TradeLine;
import cn.ynu.campus.relife.trade.domain.TradeOrder;
import cn.ynu.campus.relife.trade.dto.CancelTradeRequest;
import cn.ynu.campus.relife.trade.dto.TradeDetailVO;
import cn.ynu.campus.relife.trade.dto.TradeLineDetailVO;
import cn.ynu.campus.relife.trade.dto.TradeSummaryVO;
import cn.ynu.campus.relife.trade.mapper.TradeLineMapper;
import cn.ynu.campus.relife.trade.mapper.TradeOrderMapper;
import cn.ynu.campus.relife.trade.support.TradeOrderRepository;
import cn.ynu.campus.relife.trade.support.TradeStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradeOrderService {

    private static final int POINTS_PER_TRADE = 10;

    private final TradeOrderRepository tradeOrderRepository;
    private final TradeOrderMapper tradeOrderMapper;
    private final TradeLineMapper tradeLineMapper;
    private final StockFeignClient stockFeignClient;
    private final UserFeignClient userFeignClient;
    private final NotificationService notificationService;

    public TradeOrderService(TradeOrderRepository tradeOrderRepository,
                             TradeOrderMapper tradeOrderMapper,
                             TradeLineMapper tradeLineMapper,
                             StockFeignClient stockFeignClient,
                             UserFeignClient userFeignClient,
                             NotificationService notificationService) {
        this.tradeOrderRepository = tradeOrderRepository;
        this.tradeOrderMapper = tradeOrderMapper;
        this.tradeLineMapper = tradeLineMapper;
        this.stockFeignClient = stockFeignClient;
        this.userFeignClient = userFeignClient;
        this.notificationService = notificationService;
    }

    public TradeDetailVO getDetail(String tradeNo, Long userId) {
        TradeOrder order = tradeOrderRepository.requireByTradeNo(tradeNo);
        requireParticipant(order, userId);
        return toDetail(order);
    }

    public PageResult<TradeSummaryVO> listBuyer(Long buyerId, int page, int size, Integer status) {
        validatePage(page, size);
        long offset = (long) (page - 1) * size;
        List<TradeOrder> orders = tradeOrderMapper.selectByBuyer(buyerId, offset, size);
        long total = tradeOrderMapper.countByBuyer(buyerId);
        List<TradeSummaryVO> list = orders.stream()
                .filter(o -> status == null || status.equals(o.getStatus()))
                .map(this::toSummary)
                .toList();
        return new PageResult<>(list, page, size, total);
    }

    public PageResult<TradeSummaryVO> listSeller(Long sellerId, int page, int size, Integer status) {
        validatePage(page, size);
        long offset = (long) (page - 1) * size;
        List<TradeOrder> orders = tradeOrderMapper.selectBySeller(sellerId, offset, size);
        long total = tradeOrderMapper.countBySeller(sellerId);
        if (status != null) {
            orders = orders.stream().filter(o -> status.equals(o.getStatus())).toList();
        }
        List<TradeSummaryVO> list = orders.stream().map(this::toSummary).toList();
        return new PageResult<>(list, page, size, total);
    }

    @Transactional
    public TradeDetailVO confirm(String tradeNo, Long sellerId) {
        TradeOrder order = tradeOrderRepository.requireByTradeNo(tradeNo);
        if (!sellerId.equals(order.getSellerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_NOT_OWNER);
        }
        if (order.getStatus() == null || order.getStatus() != TradeStatus.PENDING) {
            throw new BusinessException(ErrorCode.TRADE_STATUS_ERROR);
        }
        order.setStatus(TradeStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        tradeOrderRepository.update(order);
        notificationService.send(order.getBuyerId(), order.getId(), order.getTradeNo(),
                "ORDER_CONFIRMED", "卖家已确认交易", "订单 " + order.getTradeNo() + " 已确认，请线下交易后确认收货");
        return toDetail(order);
    }

    @Transactional
    public TradeDetailVO complete(String tradeNo, Long buyerId) {
        TradeOrder order = tradeOrderRepository.requireByTradeNo(tradeNo);
        if (!buyerId.equals(order.getBuyerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_NOT_OWNER);
        }
        if (order.getStatus() == null || order.getStatus() != TradeStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.TRADE_STATUS_ERROR);
        }
        order.setStatus(TradeStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        tradeOrderRepository.update(order);
        grantTradePoints(order);
        notificationService.send(order.getSellerId(), order.getId(), order.getTradeNo(),
                "ORDER_COMPLETED", "交易已完成", "订单 " + order.getTradeNo() + " 已完成");
        return toDetail(order);
    }

    @Transactional
    public TradeDetailVO cancel(String tradeNo, Long userId, CancelTradeRequest request) {
        TradeOrder order = tradeOrderRepository.requireByTradeNo(tradeNo);
        requireParticipant(order, userId);
        int status = order.getStatus() != null ? order.getStatus() : TradeStatus.PENDING;
        if (status == TradeStatus.PENDING) {
            // buyer or seller allowed
        } else if (status == TradeStatus.CONFIRMED) {
            if (!userId.equals(order.getSellerId())) {
                throw new BusinessException(ErrorCode.TRADE_STATUS_ERROR);
            }
        } else {
            throw new BusinessException(ErrorCode.TRADE_STATUS_ERROR);
        }
        order.setStatus(TradeStatus.CANCELLED);
        order.setCancelReason(request != null && StringUtils.hasText(request.getReason())
                ? request.getReason() : "");
        tradeOrderRepository.update(order);
        releaseStock(order);
        Long other = userId.equals(order.getBuyerId()) ? order.getSellerId() : order.getBuyerId();
        notificationService.send(other, order.getId(), order.getTradeNo(),
                "ORDER_CANCELLED", "交易已取消", "订单 " + order.getTradeNo() + " 已取消");
        return toDetail(order);
    }

    @Transactional
    public void expire(TradeOrder order) {
        if (order.getStatus() == null || order.getStatus() != TradeStatus.PENDING) {
            return;
        }
        order.setStatus(TradeStatus.EXPIRED);
        tradeOrderRepository.update(order);
        releaseStock(order);
        notificationService.send(order.getBuyerId(), order.getId(), order.getTradeNo(),
                "ORDER_EXPIRED", "交易已超时", "订单 " + order.getTradeNo() + " 已超时关闭");
        notificationService.send(order.getSellerId(), order.getId(), order.getTradeNo(),
                "ORDER_EXPIRED", "交易已超时", "订单 " + order.getTradeNo() + " 已超时关闭");
    }

    public void notifyOrderCreated(TradeOrder order, String itemTitle) {
        notificationService.send(order.getSellerId(), order.getId(), order.getTradeNo(),
                "ORDER_CREATED", "您有新的交易请求",
                "买家下单：" + (itemTitle != null ? itemTitle : "闲置物品"));
    }

    private void grantTradePoints(TradeOrder order) {
        grantPoints(order.getBuyerId(), order.getId(), "TRADE_BUYER", "交易完成奖励（买家）");
        grantPoints(order.getSellerId(), order.getId(), "TRADE_SELLER", "交易完成奖励（卖家）");
    }

    private void grantPoints(Long accountId, Long tradeId, String ruleCode, String remark) {
        String idempotentKey = tradeId + "_" + ruleCode;
        ApiResponse<FeignPointGrantResultVO> response = userFeignClient.grantPoints(
                new FeignPointGrantRequest(accountId, tradeId, POINTS_PER_TRADE, ruleCode, idempotentKey, remark));
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
        }
    }

    private void releaseStock(TradeOrder order) {
        List<TradeLine> lines = loadLines(order);
        for (TradeLine line : lines) {
            ApiResponse<Void> response = stockFeignClient.release(
                    new FeignReleaseStockRequest(order.getId(), line.getItemId()));
            if (response == null || response.getCode() != 0) {
                throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
            }
        }
    }

    private List<TradeLine> loadLines(TradeOrder order) {
        return tradeLineMapper.selectByTradeId(order.getId(), order.getBuyerId());
    }

    private TradeDetailVO toDetail(TradeOrder order) {
        List<TradeLine> lines = loadLines(order);
        TradeDetailVO vo = new TradeDetailVO();
        vo.setTradeNo(order.getTradeNo());
        vo.setTradeId(order.getId());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setBuyerContact(order.getBuyerContact());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusText(TradeStatus.text(order.getStatus() != null ? order.getStatus() : TradeStatus.PENDING));
        vo.setRemark("");
        vo.setConfirmedAt(order.getConfirmedAt());
        vo.setCompletedAt(order.getCompletedAt());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setLines(lines.stream().map(line -> {
            TradeLineDetailVO lineVo = new TradeLineDetailVO();
            lineVo.setLineId(line.getId());
            lineVo.setItemId(line.getItemId());
            lineVo.setItemTitle(line.getItemTitle());
            lineVo.setItemCover(line.getItemCover());
            lineVo.setUnitPrice(line.getUnitPrice());
            lineVo.setQuantity(line.getQuantity());
            lineVo.setLineAmount(line.getLineAmount());
            return lineVo;
        }).toList());
        return vo;
    }

    private TradeSummaryVO toSummary(TradeOrder order) {
        List<TradeLine> lines = loadLines(order);
        TradeSummaryVO vo = new TradeSummaryVO();
        vo.setTradeNo(order.getTradeNo());
        vo.setTradeId(order.getId());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusText(TradeStatus.text(order.getStatus() != null ? order.getStatus() : TradeStatus.PENDING));
        vo.setItemCount(lines.size());
        vo.setCoverUrl(lines.isEmpty() ? "" : lines.get(0).getItemCover());
        vo.setCreatedAt(order.getCreatedAt());
        return vo;
    }

    private void requireParticipant(TradeOrder order, Long userId) {
        if (!userId.equals(order.getBuyerId()) && !userId.equals(order.getSellerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_NOT_OWNER);
        }
    }

    private void validatePage(int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.PAGE_INVALID);
        }
    }
}
