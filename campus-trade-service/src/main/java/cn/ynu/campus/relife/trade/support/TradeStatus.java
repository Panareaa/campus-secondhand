package cn.ynu.campus.relife.trade.support;

public final class TradeStatus {

    public static final int PENDING = 0;
    public static final int CONFIRMED = 1;
    public static final int COMPLETED = 2;
    public static final int CANCELLED = 3;
    public static final int EXPIRED = 4;

    private TradeStatus() {
    }

    public static String text(int status) {
        return switch (status) {
            case PENDING -> "PENDING";
            case CONFIRMED -> "CONFIRMED";
            case COMPLETED -> "COMPLETED";
            case CANCELLED -> "CANCELLED";
            case EXPIRED -> "EXPIRED";
            default -> "UNKNOWN";
        };
    }
}
