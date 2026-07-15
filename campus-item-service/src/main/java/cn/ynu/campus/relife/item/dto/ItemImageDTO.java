package cn.ynu.campus.relife.item.dto;

import jakarta.validation.constraints.NotBlank;

public class ItemImageDTO {

    @NotBlank
    private String imageUrl;
    private Boolean isCover = false;
    private Integer sortOrder = 0;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsCover() {
        return isCover;
    }

    public void setIsCover(Boolean isCover) {
        this.isCover = isCover;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
