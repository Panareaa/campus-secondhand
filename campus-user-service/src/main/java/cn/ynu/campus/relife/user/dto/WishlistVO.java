package cn.ynu.campus.relife.user.dto;

import java.time.LocalDateTime;

public class WishlistVO {

    private Long wishlistId;
    private Long itemId;
    private String itemTitle;
    private String itemCover;
    private java.math.BigDecimal salePrice;
    private Integer itemStatus;
    private Boolean valid;
    private LocalDateTime createdAt;

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

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemCover() {
        return itemCover;
    }

    public void setItemCover(String itemCover) {
        this.itemCover = itemCover;
    }

    public java.math.BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(java.math.BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(Integer itemStatus) {
        this.itemStatus = itemStatus;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
