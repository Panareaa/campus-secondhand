package cn.ynu.campus.relife.user.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.common.core.result.PageResult;
import cn.ynu.campus.relife.user.client.ItemFeignClient;
import cn.ynu.campus.relife.user.client.TradeFeignClient;
import cn.ynu.campus.relife.user.client.dto.FeignAddCartItemVO;
import cn.ynu.campus.relife.user.client.dto.FeignBatchSaleCheckRequest;
import cn.ynu.campus.relife.user.client.dto.FeignInternalAddCartRequest;
import cn.ynu.campus.relife.user.client.dto.FeignItemSaleCheckVO;
import cn.ynu.campus.relife.user.domain.Wishlist;
import cn.ynu.campus.relife.user.dto.AddWishlistRequest;
import cn.ynu.campus.relife.user.dto.MoveToCartRequest;
import cn.ynu.campus.relife.user.dto.MoveToCartVO;
import cn.ynu.campus.relife.user.dto.WishlistItemVO;
import cn.ynu.campus.relife.user.dto.WishlistVO;
import cn.ynu.campus.relife.user.mapper.WishlistMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final WishlistMapper wishlistMapper;
    private final TradeFeignClient tradeFeignClient;
    private final ItemFeignClient itemFeignClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WishlistService(WishlistMapper wishlistMapper,
                           TradeFeignClient tradeFeignClient,
                           ItemFeignClient itemFeignClient) {
        this.wishlistMapper = wishlistMapper;
        this.tradeFeignClient = tradeFeignClient;
        this.itemFeignClient = itemFeignClient;
    }

    public PageResult<WishlistVO> list(Long userId, int page, int size) {
        Page<Wishlist> pageQuery = new Page<>(page, size);
        Page<Wishlist> result = wishlistMapper.selectPage(pageQuery, new LambdaQueryWrapper<Wishlist>()
                .eq(Wishlist::getAccountId, userId)
                .orderByDesc(Wishlist::getCreatedAt));
        Map<Long, FeignItemSaleCheckVO> itemMap = loadItemMap(result.getRecords());
        List<WishlistVO> list = result.getRecords().stream()
                .map(w -> toVO(w, itemMap.get(w.getItemId())))
                .toList();
        return new PageResult<>(list, page, size, result.getTotal());
    }

    @Transactional
    public WishlistItemVO add(Long userId, AddWishlistRequest request) {
        long count = wishlistMapper.selectCount(new LambdaQueryWrapper<Wishlist>()
                .eq(Wishlist::getAccountId, userId)
                .eq(Wishlist::getItemId, request.getItemId()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.RESOURCE_EXISTS);
        }
        Wishlist wishlist = new Wishlist();
        wishlist.setAccountId(userId);
        wishlist.setItemId(request.getItemId());
        wishlistMapper.insert(wishlist);
        return new WishlistItemVO(wishlist.getId(), wishlist.getItemId());
    }

    @Transactional
    public void remove(Long userId, Long itemId) {
        int deleted = wishlistMapper.delete(new LambdaQueryWrapper<Wishlist>()
                .eq(Wishlist::getAccountId, userId)
                .eq(Wishlist::getItemId, itemId));
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
    }

    @Transactional
    public MoveToCartVO moveToCart(Long userId, Long itemId, MoveToCartRequest request) {
        long count = wishlistMapper.selectCount(new LambdaQueryWrapper<Wishlist>()
                .eq(Wishlist::getAccountId, userId)
                .eq(Wishlist::getItemId, itemId));
        if (count == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        ApiResponse<FeignAddCartItemVO> response;
        try {
            response = tradeFeignClient.addCartItem(
                    new FeignInternalAddCartRequest(userId, itemId, 1));
        } catch (FeignException ex) {
            throw mapFeignException(ex);
        }
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
        }
        boolean keepWishlist = request != null && Boolean.TRUE.equals(request.getKeepWishlist());
        if (!keepWishlist) {
            wishlistMapper.delete(new LambdaQueryWrapper<Wishlist>()
                    .eq(Wishlist::getAccountId, userId)
                    .eq(Wishlist::getItemId, itemId));
        }
        FeignAddCartItemVO data = response.getData();
        return new MoveToCartVO(data.getCartEntryId(), data.getItemId());
    }

    private Map<Long, FeignItemSaleCheckVO> loadItemMap(List<Wishlist> records) {
        if (records.isEmpty()) {
            return Map.of();
        }
        List<Long> itemIds = records.stream().map(Wishlist::getItemId).distinct().toList();
        ApiResponse<List<FeignItemSaleCheckVO>> response =
                itemFeignClient.batchSaleCheck(new FeignBatchSaleCheckRequest(itemIds));
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            return Map.of();
        }
        return response.getData().stream()
                .collect(Collectors.toMap(FeignItemSaleCheckVO::getItemId, v -> v, (a, b) -> a, HashMap::new));
    }

    private WishlistVO toVO(Wishlist wishlist, FeignItemSaleCheckVO item) {
        WishlistVO vo = new WishlistVO();
        vo.setWishlistId(wishlist.getId());
        vo.setItemId(wishlist.getItemId());
        vo.setCreatedAt(wishlist.getCreatedAt());
        if (item != null) {
            vo.setItemTitle(item.getTitle());
            vo.setItemCover(item.getCoverUrl());
            vo.setSalePrice(item.getSalePrice());
            vo.setItemStatus(item.getStatus());
            vo.setValid(Boolean.TRUE.equals(item.getOnSale()));
        } else {
            vo.setValid(false);
        }
        return vo;
    }

    private BusinessException mapFeignException(FeignException ex) {
        try {
            String body = ex.contentUTF8();
            if (body != null && !body.isBlank()) {
                JsonNode node = objectMapper.readTree(body);
                if (node.has("code") && node.has("message")) {
                    return new BusinessException(node.get("code").asInt(), node.get("message").asText());
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
    }
}
