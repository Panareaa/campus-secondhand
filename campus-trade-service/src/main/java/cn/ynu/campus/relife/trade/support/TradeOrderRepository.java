package cn.ynu.campus.relife.trade.support;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.trade.domain.TradeOrder;
import cn.ynu.campus.relife.trade.mapper.TradeOrderMapper;
import org.springframework.stereotype.Component;

@Component
public class TradeOrderRepository {

    private final TradeOrderMapper tradeOrderMapper;

    public TradeOrderRepository(TradeOrderMapper tradeOrderMapper) {
        this.tradeOrderMapper = tradeOrderMapper;
    }

    public TradeOrder requireByTradeNo(String tradeNo) {
        TradeOrder order = tradeOrderMapper.selectByTradeNo(tradeNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.TRADE_NOT_FOUND);
        }
        return order;
    }

    public TradeOrder findByTradeNo(String tradeNo) {
        return tradeOrderMapper.selectByTradeNo(tradeNo);
    }

    public String orderTable(long buyerId) {
        return TradeShardHelper.orderTable(buyerId);
    }

    public String lineTable(long buyerId) {
        return TradeShardHelper.lineTable(buyerId);
    }

    public void update(TradeOrder order) {
        tradeOrderMapper.updateOrder(order);
    }
}
