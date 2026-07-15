package cn.ynu.campus.relife.trade.dto;

public class AddCartItemVO {

    private Long cartEntryId;
    private Long itemId;
    private Integer quantity;

    public AddCartItemVO() {
    }

    public AddCartItemVO(Long cartEntryId, Long itemId, Integer quantity) {
        this.cartEntryId = cartEntryId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

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
