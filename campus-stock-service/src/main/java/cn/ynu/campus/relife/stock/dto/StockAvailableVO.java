package cn.ynu.campus.relife.stock.dto;

public class StockAvailableVO {

    private Long itemId;
    private Integer totalQty;
    private Integer lockedQty;
    private Integer availableQty;

    public StockAvailableVO() {
    }

    public StockAvailableVO(Long itemId, Integer totalQty, Integer lockedQty, Integer availableQty) {
        this.itemId = itemId;
        this.totalQty = totalQty;
        this.lockedQty = lockedQty;
        this.availableQty = availableQty;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(Integer totalQty) {
        this.totalQty = totalQty;
    }

    public Integer getLockedQty() {
        return lockedQty;
    }

    public void setLockedQty(Integer lockedQty) {
        this.lockedQty = lockedQty;
    }

    public Integer getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(Integer availableQty) {
        this.availableQty = availableQty;
    }
}
