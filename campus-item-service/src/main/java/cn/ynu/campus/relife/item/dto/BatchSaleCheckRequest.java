package cn.ynu.campus.relife.item.dto;

import java.math.BigDecimal;
import java.util.List;

public class BatchSaleCheckRequest {

    private List<Long> itemIds;

    public List<Long> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Long> itemIds) {
        this.itemIds = itemIds;
    }
}
