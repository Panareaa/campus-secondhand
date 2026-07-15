package cn.ynu.campus.relife.user.dto;

public class WishlistItemVO {

    private Long wishlistId;
    private Long itemId;

    public WishlistItemVO() {
    }

    public WishlistItemVO(Long wishlistId, Long itemId) {
        this.wishlistId = wishlistId;
        this.itemId = itemId;
    }

    public Long getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(Long wishlistId) {
        this.wishlistId = wishlistId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}
