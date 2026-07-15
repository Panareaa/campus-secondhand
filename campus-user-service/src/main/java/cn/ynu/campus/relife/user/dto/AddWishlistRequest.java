package cn.ynu.campus.relife.user.dto;

import jakarta.validation.constraints.NotNull;

public class AddWishlistRequest {

    @NotNull
    private Long itemId;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}
