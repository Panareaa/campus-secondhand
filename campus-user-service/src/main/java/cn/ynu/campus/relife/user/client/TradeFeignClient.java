package cn.ynu.campus.relife.user.client;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.user.client.dto.FeignAddCartItemVO;
import cn.ynu.campus.relife.user.client.dto.FeignInternalAddCartRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "campus-trade-service")
public interface TradeFeignClient {

    @PostMapping("/internal/cart/items")
    ApiResponse<FeignAddCartItemVO> addCartItem(@RequestBody FeignInternalAddCartRequest request);
}
