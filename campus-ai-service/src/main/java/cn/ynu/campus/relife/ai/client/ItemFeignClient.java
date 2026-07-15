package cn.ynu.campus.relife.ai.client;

import cn.ynu.campus.relife.ai.client.dto.FeignInternalItemSearchRequest;
import cn.ynu.campus.relife.ai.client.dto.FeignItemSummaryVO;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.common.core.result.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "campus-item-service")
public interface ItemFeignClient {

    @PostMapping("/internal/items/search")
    ApiResponse<PageResult<FeignItemSummaryVO>> search(@RequestBody FeignInternalItemSearchRequest request);
}
