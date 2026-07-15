package cn.ynu.campus.relife.user.controller;

import cn.ynu.campus.relife.common.core.constant.GatewayHeaders;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.common.core.result.PageResult;
import cn.ynu.campus.relife.user.dto.AddWishlistRequest;
import cn.ynu.campus.relife.user.dto.MoveToCartRequest;
import cn.ynu.campus.relife.user.dto.MoveToCartVO;
import cn.ynu.campus.relife.user.dto.WishlistItemVO;
import cn.ynu.campus.relife.user.dto.WishlistVO;
import cn.ynu.campus.relife.user.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ApiResponse<PageResult<WishlistVO>> list(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(wishlistService.list(userId, page, size));
    }

    @PostMapping
    public ApiResponse<WishlistItemVO> add(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                           @Valid @RequestBody AddWishlistRequest request) {
        return ApiResponse.ok(wishlistService.add(userId, request));
    }

    @DeleteMapping("/{itemId}")
    public ApiResponse<Void> remove(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                    @PathVariable Long itemId) {
        wishlistService.remove(userId, itemId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{itemId}/move-to-cart")
    public ApiResponse<MoveToCartVO> moveToCart(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                                @PathVariable Long itemId,
                                                @RequestBody(required = false) MoveToCartRequest request) {
        return ApiResponse.ok(wishlistService.moveToCart(userId, itemId, request));
    }
}
