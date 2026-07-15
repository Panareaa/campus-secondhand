package cn.ynu.campus.relife.trade.client;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.trade.client.dto.FeignPointGrantRequest;
import cn.ynu.campus.relife.trade.client.dto.FeignPointGrantResultVO;
import cn.ynu.campus.relife.trade.client.dto.FeignUserValidateVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "campus-user-service", fallback = UserFeignClientFallback.class)
public interface UserFeignClient {

    @GetMapping("/internal/users/{userId}/validate")
    ApiResponse<FeignUserValidateVO> validate(@PathVariable("userId") Long userId);

    @PostMapping("/internal/points/grant")
    ApiResponse<FeignPointGrantResultVO> grantPoints(@RequestBody FeignPointGrantRequest request);
}
