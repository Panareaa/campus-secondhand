package cn.ynu.campus.relife.item.controller;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("service", "campus-item-service", "status", "UP"));
    }
}
