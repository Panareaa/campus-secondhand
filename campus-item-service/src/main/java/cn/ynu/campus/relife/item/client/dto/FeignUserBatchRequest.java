package cn.ynu.campus.relife.item.client.dto;

import java.util.List;
import java.util.Map;

public class FeignUserBatchRequest {

    private List<Long> userIds;

    public FeignUserBatchRequest() {
    }

    public FeignUserBatchRequest(List<Long> userIds) {
        this.userIds = userIds;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
}
