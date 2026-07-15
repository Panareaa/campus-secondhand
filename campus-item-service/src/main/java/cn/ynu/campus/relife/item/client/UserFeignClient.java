package cn.ynu.campus.relife.item.client;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.item.client.dto.FeignUserBatchRequest;
import cn.ynu.campus.relife.item.client.dto.FeignUserBriefVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "campus-user-service")
public interface UserFeignClient {

    @PostMapping("/internal/users/batch")
    ApiResponse<Map<String, FeignUserBriefVO>> batch(@RequestBody FeignUserBatchRequest request);
}
