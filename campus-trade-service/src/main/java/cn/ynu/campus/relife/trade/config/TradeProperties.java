package cn.ynu.campus.relife.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "campus.trade")
public class TradeProperties {

    /** PENDING 订单超时分钟数，默认 24 小时 */
    private int pendingTimeoutMinutes = 1440;

    /** 超时关单扫描间隔（毫秒） */
    private long timeoutScanIntervalMs = 60000;

    public int getPendingTimeoutMinutes() {
        return pendingTimeoutMinutes;
    }

    public void setPendingTimeoutMinutes(int pendingTimeoutMinutes) {
        this.pendingTimeoutMinutes = pendingTimeoutMinutes;
    }

    public long getTimeoutScanIntervalMs() {
        return timeoutScanIntervalMs;
    }

    public void setTimeoutScanIntervalMs(long timeoutScanIntervalMs) {
        this.timeoutScanIntervalMs = timeoutScanIntervalMs;
    }
}
