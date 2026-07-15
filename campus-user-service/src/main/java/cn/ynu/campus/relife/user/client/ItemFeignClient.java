package cn.ynu.campus.relife.user.client;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.user.client.dto.FeignBatchSaleCheckRequest;
import cn.ynu.campus.relife.user.client.dto.FeignItemSaleCheckVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "campus-item-service")
public interface ItemFeignClient {

    @PostMapping("/internal/items/sale-check/batch")
    ApiResponse<List<FeignItemSaleCheckVO>> batchSaleCheck(@RequestBody FeignBatchSaleCheckRequest request);
}
