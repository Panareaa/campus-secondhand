package cn.ynu.campus.relife.trade.job;

import cn.ynu.campus.relife.trade.domain.TradeOrder;
import cn.ynu.campus.relife.trade.mapper.TradeOrderMapper;
import cn.ynu.campus.relife.trade.service.TradeOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TradeTimeoutJob {

    private static final Logger log = LoggerFactory.getLogger(TradeTimeoutJob.class);

    private final TradeOrderMapper tradeOrderMapper;
    private final TradeOrderService tradeOrderService;

    public TradeTimeoutJob(TradeOrderMapper tradeOrderMapper,
                           TradeOrderService tradeOrderService) {
        this.tradeOrderMapper = tradeOrderMapper;
        this.tradeOrderService = tradeOrderService;
    }

    @Scheduled(fixedDelayString = "${campus.trade.timeout-scan-interval-ms:60000}")
    public void scanExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<TradeOrder> expiredOrders = tradeOrderMapper.selectPendingExpired(now, 50);
        for (TradeOrder order : expiredOrders) {
            try {
                tradeOrderService.expire(order);
                log.info("Expired pending order tradeNo={} buyerId={}", order.getTradeNo(), order.getBuyerId());
            } catch (Exception ex) {
                log.warn("Failed to expire order tradeNo={}: {}", order.getTradeNo(), ex.getMessage());
            }
        }
    }
}
