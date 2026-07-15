package cn.ynu.campus.relife.user.controller;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.user.dto.AuthTokenResponse;
import cn.ynu.campus.relife.user.dto.LoginRequest;
import cn.ynu.campus.relife.user.dto.RegisterRequest;
import cn.ynu.campus.relife.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthTokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
