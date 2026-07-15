package cn.ynu.campus.relife.stock.dto;

import jakarta.validation.constraints.NotNull;

public class ReleaseStockRequest {

    @NotNull
    private Long tradeId;

    @NotNull
    private Long itemId;

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
