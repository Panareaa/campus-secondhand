package cn.ynu.campus.relife.item.client.dto;

public class FeignInitStockRequest {

    private Long itemId;
    private Integer totalQty;

    public FeignInitStockRequest() {
    }

    public FeignInitStockRequest(Long itemId, Integer totalQty) {
        this.itemId = itemId;
        this.totalQty = totalQty;
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
}
