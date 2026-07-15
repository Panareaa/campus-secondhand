package cn.ynu.campus.relife.item.dto;

public class ItemImageVO {

    private String imageUrl;
    private Boolean isCover;
    private Integer sortOrder;

    public ItemImageVO() {
    }

    public ItemImageVO(String imageUrl, Boolean isCover, Integer sortOrder) {
        this.imageUrl = imageUrl;
        this.isCover = isCover;
        this.sortOrder = sortOrder;
    }

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
