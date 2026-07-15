package cn.ynu.campus.relife.user.client.dto;

public class FeignInternalAddCartRequest {

    private Long buyerId;
    private Long itemId;
    private Integer quantity = 1;

    public FeignInternalAddCartRequest() {
    }

    public FeignInternalAddCartRequest(Long buyerId, Long itemId, Integer quantity) {
        this.buyerId = buyerId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

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
