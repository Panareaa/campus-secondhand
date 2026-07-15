package cn.ynu.campus.relife.user.controller;

import cn.ynu.campus.relife.common.core.constant.GatewayHeaders;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.common.core.result.PageResult;
import cn.ynu.campus.relife.user.dto.PointBalanceVO;
import cn.ynu.campus.relife.user.dto.PointLedgerVO;
import cn.ynu.campus.relife.user.service.PointService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    private final PointService pointService;

    public PointsController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping("/me")
    public ApiResponse<PointBalanceVO> me(@RequestHeader(GatewayHeaders.USER_ID) Long userId) {
        return ApiResponse.ok(pointService.getBalance(userId));
    }

    @GetMapping("/me/ledger")
    public ApiResponse<PageResult<PointLedgerVO>> ledger(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(pointService.listLedger(userId, page, size));
    }
}
