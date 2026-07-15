package cn.ynu.campus.relife.user.dto;

public class MoveToCartVO {

    private Long cartEntryId;
    private Long itemId;

    public MoveToCartVO() {
    }

    public MoveToCartVO(Long cartEntryId, Long itemId) {
        this.cartEntryId = cartEntryId;
        this.itemId = itemId;
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
}
