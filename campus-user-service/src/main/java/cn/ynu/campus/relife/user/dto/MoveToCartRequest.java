package cn.ynu.campus.relife.user.dto;

public class MoveToCartRequest {

    private Boolean keepWishlist = false;

    public Boolean getKeepWishlist() {
        return keepWishlist;
    }

    public void setKeepWishlist(Boolean keepWishlist) {
        this.keepWishlist = keepWishlist;
    }
}
