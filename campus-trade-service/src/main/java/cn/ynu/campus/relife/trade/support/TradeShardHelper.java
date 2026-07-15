package cn.ynu.campus.relife.trade.support;

public final class TradeShardHelper {

    private TradeShardHelper() {
    }

    public static String orderTable(long buyerId) {
        return "trade_order_" + (buyerId % 2);
    }

    public static String lineTable(long buyerId) {
        return "trade_line_" + (buyerId % 2);
    }
}
