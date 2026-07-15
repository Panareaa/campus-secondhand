package cn.ynu.campus.relife.trade.client.dto;

public class FeignReleaseStockRequest {

    private Long tradeId;
    private Long itemId;

    public FeignReleaseStockRequest() {
    }

    public FeignReleaseStockRequest(Long tradeId, Long itemId) {
        this.tradeId = tradeId;
        this.itemId = itemId;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}
