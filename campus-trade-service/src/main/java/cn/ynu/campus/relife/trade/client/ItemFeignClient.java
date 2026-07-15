package cn.ynu.campus.relife.trade.client;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.trade.client.dto.FeignBatchSaleCheckRequest;
import cn.ynu.campus.relife.trade.client.dto.FeignItemSaleCheckVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "campus-item-service", fallback = ItemFeignClientFallback.class)
public interface ItemFeignClient {

    @GetMapping("/internal/items/{itemId}/sale-check")
    ApiResponse<FeignItemSaleCheckVO> saleCheck(@PathVariable("itemId") Long itemId);

    @PostMapping("/internal/items/sale-check/batch")
    ApiResponse<List<FeignItemSaleCheckVO>> batchSaleCheck(@RequestBody FeignBatchSaleCheckRequest request);
}
