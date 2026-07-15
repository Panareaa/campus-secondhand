package cn.ynu.campus.relife.trade.controller;

import cn.ynu.campus.relife.common.core.constant.GatewayHeaders;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.trade.dto.AddCartItemRequest;
import cn.ynu.campus.relife.trade.dto.AddCartItemVO;
import cn.ynu.campus.relife.trade.dto.CartVO;
import cn.ynu.campus.relife.trade.dto.SettlePreviewVO;
import cn.ynu.campus.relife.trade.dto.UpdateCartItemRequest;
import cn.ynu.campus.relife.trade.service.CartService;
import cn.ynu.campus.relife.trade.service.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final CheckoutService checkoutService;

    public CartController(CartService cartService, CheckoutService checkoutService) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
    }

    @GetMapping
    public ApiResponse<CartVO> list(@RequestHeader(GatewayHeaders.USER_ID) Long userId) {
        return ApiResponse.ok(cartService.list(userId));
    }

    @GetMapping("/settle-preview")
    public ApiResponse<SettlePreviewVO> settlePreview(@RequestHeader(GatewayHeaders.USER_ID) Long userId) {
        return ApiResponse.ok(checkoutService.settlePreview(userId));
    }

    @PostMapping("/items")
    public ApiResponse<AddCartItemVO> add(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                          @Valid @RequestBody AddCartItemRequest request) {
        return ApiResponse.ok(cartService.add(userId, request));
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<AddCartItemVO> update(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody UpdateCartItemRequest request) {
        return ApiResponse.ok(cartService.updateQuantity(userId, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Void> remove(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                    @PathVariable Long itemId) {
        cartService.remove(userId, itemId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping
    public ApiResponse<Void> clear(@RequestHeader(GatewayHeaders.USER_ID) Long userId) {
        cartService.clear(userId);
        return ApiResponse.ok(null);
    }
}
