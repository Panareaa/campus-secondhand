package cn.ynu.campus.relife.trade.client;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.trade.client.dto.FeignLockStockRequest;
import cn.ynu.campus.relife.trade.client.dto.FeignReleaseStockRequest;
import cn.ynu.campus.relife.trade.client.dto.FeignStockAvailableVO;
import cn.ynu.campus.relife.trade.client.dto.FeignStockLockResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "campus-stock-service", fallback = StockFeignClientFallback.class)
public interface StockFeignClient {

    @GetMapping("/internal/stock/{itemId}/available")
    ApiResponse<FeignStockAvailableVO> available(@PathVariable("itemId") Long itemId);

    @PostMapping("/internal/stock/lock")
    ApiResponse<FeignStockLockResultVO> lock(@RequestBody FeignLockStockRequest request);

    @PostMapping("/internal/stock/release")
    ApiResponse<Void> release(@RequestBody FeignReleaseStockRequest request);
}
