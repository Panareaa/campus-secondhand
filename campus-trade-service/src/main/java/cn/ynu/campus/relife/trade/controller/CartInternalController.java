package cn.ynu.campus.relife.trade.controller;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.trade.dto.AddCartItemRequest;
import cn.ynu.campus.relife.trade.dto.AddCartItemVO;
import cn.ynu.campus.relife.trade.dto.InternalAddCartRequest;
import cn.ynu.campus.relife.trade.service.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/cart")
public class CartInternalController {

    private final CartService cartService;

    public CartInternalController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public ApiResponse<AddCartItemVO> addItem(@Valid @RequestBody InternalAddCartRequest request) {
        AddCartItemRequest addRequest = new AddCartItemRequest();
        addRequest.setItemId(request.getItemId());
        addRequest.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        return ApiResponse.ok(cartService.add(request.getBuyerId(), addRequest));
    }
}
