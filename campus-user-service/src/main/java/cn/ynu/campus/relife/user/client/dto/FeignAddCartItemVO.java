package cn.ynu.campus.relife.user.client.dto;

public class FeignAddCartItemVO {

    private Long cartEntryId;
    private Long itemId;
    private Integer quantity;

    public Long getCartEntryId() {
        return cartEntryId;
    }

    public void setCartEntryId(Long cartEntryId) {
        this.cartEntryId = cartEntryId;
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
