package cn.ynu.campus.relife.item.client;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.item.client.dto.FeignInitStockRequest;
import cn.ynu.campus.relife.item.client.dto.FeignStockAvailableVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "campus-stock-service")
public interface StockFeignClient {

    @PostMapping("/internal/stock/init")
    ApiResponse<FeignStockAvailableVO> initStock(@RequestBody FeignInitStockRequest request);

    @GetMapping("/internal/stock/{itemId}/available")
    ApiResponse<FeignStockAvailableVO> available(@PathVariable("itemId") Long itemId);
}
