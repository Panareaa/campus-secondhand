package cn.ynu.campus.relife.item.dto;

public class ItemStatusVO {

    private Long itemId;
    private Integer status;

    public ItemStatusVO() {
    }

    public ItemStatusVO(Long itemId, Integer status) {
        this.itemId = itemId;
        this.status = status;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
