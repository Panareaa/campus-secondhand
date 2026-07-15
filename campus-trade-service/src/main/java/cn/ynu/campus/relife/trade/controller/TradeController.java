package cn.ynu.campus.relife.trade.controller;

import cn.ynu.campus.relife.common.core.constant.GatewayHeaders;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.common.core.result.PageResult;
import cn.ynu.campus.relife.trade.dto.CancelTradeRequest;
import cn.ynu.campus.relife.trade.dto.CheckoutRequest;
import cn.ynu.campus.relife.trade.dto.CheckoutVO;
import cn.ynu.campus.relife.trade.dto.NotificationVO;
import cn.ynu.campus.relife.trade.dto.TradeDetailVO;
import cn.ynu.campus.relife.trade.dto.TradeSummaryVO;
import cn.ynu.campus.relife.trade.service.CheckoutService;
import cn.ynu.campus.relife.trade.service.NotificationService;
import cn.ynu.campus.relife.trade.service.TradeOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TradeController {

    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private final CheckoutService checkoutService;
    private final TradeOrderService tradeOrderService;
    private final NotificationService notificationService;

    public TradeController(CheckoutService checkoutService,
                           TradeOrderService tradeOrderService,
                           NotificationService notificationService) {
        this.checkoutService = checkoutService;
        this.tradeOrderService = tradeOrderService;
        this.notificationService = notificationService;
    }

    @PostMapping("/trades/checkout")
    public ApiResponse<CheckoutVO> checkout(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                            @RequestHeader(value = IDEMPOTENCY_KEY, required = false) String idempotencyKey,
                                            @RequestBody(required = false) CheckoutRequest request) {
        CheckoutRequest body = request != null ? request : new CheckoutRequest();
        return ApiResponse.ok(checkoutService.checkout(userId, body, idempotencyKey));
    }

    @GetMapping("/trades/buyer")
    public ApiResponse<PageResult<TradeSummaryVO>> buyerOrders(
            @RequestHeader(GatewayHeaders.USER_ID) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(tradeOrderService.listBuyer(userId, page, size, status));
    }

    @GetMapping("/trades/seller")
    public ApiResponse<PageResult<TradeSummaryVO>> sellerOrders(
            @RequestHeader(GatewayHeaders.USER_ID) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(tradeOrderService.listSeller(userId, page, size, status));
    }

    @GetMapping("/trades/{tradeNo}")
    public ApiResponse<TradeDetailVO> detail(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                             @PathVariable String tradeNo) {
        return ApiResponse.ok(tradeOrderService.getDetail(tradeNo, userId));
    }

    @PutMapping("/trades/{tradeNo}/confirm")
    public ApiResponse<TradeDetailVO> confirm(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                              @PathVariable String tradeNo) {
        return ApiResponse.ok(tradeOrderService.confirm(tradeNo, userId));
    }

    @PutMapping("/trades/{tradeNo}/complete")
    public ApiResponse<TradeDetailVO> complete(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                               @PathVariable String tradeNo) {
        return ApiResponse.ok(tradeOrderService.complete(tradeNo, userId));
    }

    @PutMapping("/trades/{tradeNo}/cancel")
    public ApiResponse<TradeDetailVO> cancel(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                             @PathVariable String tradeNo,
                                             @RequestBody(required = false) CancelTradeRequest request) {
        return ApiResponse.ok(tradeOrderService.cancel(tradeNo, userId, request));
    }

    @GetMapping("/notifications")
    public ApiResponse<PageResult<NotificationVO>> notifications(
            @RequestHeader(GatewayHeaders.USER_ID) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isRead) {
        return ApiResponse.ok(notificationService.list(userId, page, size, isRead));
    }

    @PutMapping("/notifications/{notificationId}/read")
    public ApiResponse<Void> markRead(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                      @PathVariable Long notificationId) {
        notificationService.markRead(userId, notificationId);
        return ApiResponse.ok(null);
    }

    @PutMapping("/notifications/read-all")
    public ApiResponse<Void> markAllRead(@RequestHeader(GatewayHeaders.USER_ID) Long userId) {
        notificationService.markAllRead(userId);
        return ApiResponse.ok(null);
    }
}
