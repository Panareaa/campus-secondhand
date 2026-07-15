package cn.ynu.campus.relife.trade.client.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FeignLockStockRequest {

    @NotNull
    private Long itemId;

    @NotNull
    private Long tradeId;

    @NotBlank
    private String tradeNo;

    @NotNull
    @Min(1)
    private Integer quantity = 1;

    public FeignLockStockRequest() {
    }

    public FeignLockStockRequest(Long itemId, Long tradeId, String tradeNo, Integer quantity) {
        this.itemId = itemId;
        this.tradeId = tradeId;
        this.tradeNo = tradeNo;
        this.quantity = quantity;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
