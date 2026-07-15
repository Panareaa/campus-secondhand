package cn.ynu.campus.relife.trade.client.dto;

public class FeignStockAvailableVO {

    private Long itemId;
    private Integer availableQty;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(Integer availableQty) {
        this.availableQty = availableQty;
    }
}
