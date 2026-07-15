package cn.ynu.campus.relife.user.client.dto;

import java.util.List;

public class FeignBatchSaleCheckRequest {

    private List<Long> itemIds;

    public FeignBatchSaleCheckRequest() {
    }

    public FeignBatchSaleCheckRequest(List<Long> itemIds) {
        this.itemIds = itemIds;
    }

    public List<Long> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Long> itemIds) {
        this.itemIds = itemIds;
    }
}
