package cn.ynu.campus.relife.ai.controller;

import cn.ynu.campus.relife.common.core.constant.GatewayHeaders;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.ai.dto.DescribeRequest;
import cn.ynu.campus.relife.ai.dto.DescribeResponse;
import cn.ynu.campus.relife.ai.dto.SearchRequest;
import cn.ynu.campus.relife.ai.dto.SearchResponse;
import cn.ynu.campus.relife.ai.service.AiDescribeService;
import cn.ynu.campus.relife.ai.service.AiSearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiDescribeService aiDescribeService;
    private final AiSearchService aiSearchService;

    public AiController(AiDescribeService aiDescribeService, AiSearchService aiSearchService) {
        this.aiDescribeService = aiDescribeService;
        this.aiSearchService = aiSearchService;
    }

    @PostMapping("/describe")
    public ApiResponse<DescribeResponse> describe(
            @RequestHeader(GatewayHeaders.USER_ID) Long userId,
            @Valid @RequestBody DescribeRequest request) {
        return ApiResponse.ok(aiDescribeService.describe(userId, request));
    }

    @PostMapping("/search")
    public ApiResponse<SearchResponse> search(
            @RequestHeader(value = GatewayHeaders.USER_ID, required = false) Long userId,
            @Valid @RequestBody SearchRequest request) {
        return ApiResponse.ok(aiSearchService.search(userId, request));
    }
}
