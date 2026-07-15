package cn.ynu.campus.relife.trade.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class InternalAddCartRequest {

    @NotNull
    private Long buyerId;

    @NotNull
    private Long itemId;

    @Min(1)
    private Integer quantity = 1;

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
