package cn.ynu.campus.relife.user.controller;

import cn.ynu.campus.relife.common.core.constant.GatewayHeaders;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.user.dto.CertificationRequest;
import cn.ynu.campus.relife.user.dto.CertificationVO;
import cn.ynu.campus.relife.user.dto.UpdateContactRequest;
import cn.ynu.campus.relife.user.dto.UpdateProfileRequest;
import cn.ynu.campus.relife.user.dto.UserProfileVO;
import cn.ynu.campus.relife.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileVO> getMe(@RequestHeader(GatewayHeaders.USER_ID) Long userId) {
        return ApiResponse.ok(userProfileService.getProfile(userId));
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileVO> updateMe(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                               @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(userProfileService.updateProfile(userId, request));
    }

    @PutMapping("/me/contact")
    public ApiResponse<UserProfileVO> updateContact(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                                    @Valid @RequestBody UpdateContactRequest request) {
        return ApiResponse.ok(userProfileService.updateContact(userId, request));
    }

    @PostMapping("/me/certification")
    public ApiResponse<CertificationVO> certify(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                                @Valid @RequestBody CertificationRequest request) {
        return ApiResponse.ok(userProfileService.certify(userId, request));
    }
}
