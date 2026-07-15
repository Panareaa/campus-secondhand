package cn.ynu.campus.relife.user.controller;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.user.dto.PointGrantRequest;
import cn.ynu.campus.relife.user.dto.PointGrantResultVO;
import cn.ynu.campus.relife.user.dto.UserBatchRequest;
import cn.ynu.campus.relife.user.dto.UserBriefVO;
import cn.ynu.campus.relife.user.dto.UserValidateVO;
import cn.ynu.campus.relife.user.service.PointService;
import cn.ynu.campus.relife.user.service.UserInternalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal")
public class UserInternalController {

    private final UserInternalService userInternalService;
    private final PointService pointService;

    public UserInternalController(UserInternalService userInternalService, PointService pointService) {
        this.userInternalService = userInternalService;
        this.pointService = pointService;
    }

    @GetMapping("/users/{userId}/validate")
    public ApiResponse<UserValidateVO> validate(@PathVariable Long userId) {
        return ApiResponse.ok(userInternalService.validate(userId));
    }

    @PostMapping("/users/batch")
    public ApiResponse<Map<String, UserBriefVO>> batch(@RequestBody UserBatchRequest request) {
        return ApiResponse.ok(userInternalService.batchQuery(request.getUserIds()));
    }

    @PostMapping("/points/grant")
    public ApiResponse<PointGrantResultVO> grant(@Valid @RequestBody PointGrantRequest request) {
        return ApiResponse.ok(pointService.grant(request));
    }
}
