package cn.ynu.campus.relife.item.controller;

import cn.ynu.campus.relife.common.core.constant.GatewayHeaders;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.common.core.result.PageResult;
import cn.ynu.campus.relife.item.dto.ItemDetailVO;
import cn.ynu.campus.relife.item.dto.ItemStatusVO;
import cn.ynu.campus.relife.item.dto.ItemSummaryVO;
import cn.ynu.campus.relife.item.dto.PublishItemRequest;
import cn.ynu.campus.relife.item.service.ItemQueryService;
import cn.ynu.campus.relife.item.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final ItemQueryService itemQueryService;

    public ItemController(ItemService itemService, ItemQueryService itemQueryService) {
        this.itemService = itemService;
        this.itemQueryService = itemQueryService;
    }

    @GetMapping
    public ApiResponse<PageResult<ItemSummaryVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer conditionLevel,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(itemQueryService.list(page, size, categoryId, keyword,
                minPrice, maxPrice, conditionLevel, sort, status));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResult<ItemSummaryVO>> mine(
            @RequestHeader(GatewayHeaders.USER_ID) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(itemQueryService.listMine(userId, page, size, status));
    }

    @GetMapping("/{itemId}")
    public ApiResponse<ItemDetailVO> detail(@PathVariable Long itemId) {
        return ApiResponse.ok(itemQueryService.getDetailAndIncrView(itemId));
    }

    @PostMapping
    public ApiResponse<ItemStatusVO> publish(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                             @Valid @RequestBody PublishItemRequest request) {
        return ApiResponse.ok(itemService.publish(userId, request));
    }

    @PutMapping("/{itemId}/publish")
    public ApiResponse<ItemStatusVO> publishOnShelf(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                                    @PathVariable Long itemId) {
        return ApiResponse.ok(itemService.publishItem(userId, itemId));
    }

    @PutMapping("/{itemId}/off-shelf")
    public ApiResponse<ItemStatusVO> offShelf(@RequestHeader(GatewayHeaders.USER_ID) Long userId,
                                              @PathVariable Long itemId) {
        return ApiResponse.ok(itemService.offShelf(userId, itemId));
    }
}
