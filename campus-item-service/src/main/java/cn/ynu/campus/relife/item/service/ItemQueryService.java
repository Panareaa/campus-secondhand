package cn.ynu.campus.relife.item.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.common.core.result.PageResult;
import cn.ynu.campus.relife.item.client.StockFeignClient;
import cn.ynu.campus.relife.item.client.UserFeignClient;
import cn.ynu.campus.relife.item.client.dto.FeignStockAvailableVO;
import cn.ynu.campus.relife.item.client.dto.FeignUserBatchRequest;
import cn.ynu.campus.relife.item.client.dto.FeignUserBriefVO;
import cn.ynu.campus.relife.item.domain.IdleItem;
import cn.ynu.campus.relife.item.domain.ItemCategory;
import cn.ynu.campus.relife.item.domain.ItemImage;
import cn.ynu.campus.relife.item.dto.ItemDetailVO;
import cn.ynu.campus.relife.item.dto.ItemImageVO;
import cn.ynu.campus.relife.item.dto.ItemSummaryVO;
import cn.ynu.campus.relife.item.mapper.IdleItemMapper;
import cn.ynu.campus.relife.item.mapper.ItemCategoryMapper;
import cn.ynu.campus.relife.item.mapper.ItemImageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemQueryService {

    private static final int STATUS_ON_SALE = 1;

    private final IdleItemMapper idleItemMapper;
    private final ItemCategoryMapper itemCategoryMapper;
    private final ItemImageMapper itemImageMapper;
    private final StockFeignClient stockFeignClient;
    private final UserFeignClient userFeignClient;
    private final ItemCacheService itemCacheService;

    public ItemQueryService(IdleItemMapper idleItemMapper,
                            ItemCategoryMapper itemCategoryMapper,
                            ItemImageMapper itemImageMapper,
                            StockFeignClient stockFeignClient,
                            UserFeignClient userFeignClient,
                            ItemCacheService itemCacheService) {
        this.idleItemMapper = idleItemMapper;
        this.itemCategoryMapper = itemCategoryMapper;
        this.itemImageMapper = itemImageMapper;
        this.stockFeignClient = stockFeignClient;
        this.userFeignClient = userFeignClient;
        this.itemCacheService = itemCacheService;
    }

    @Transactional(readOnly = true)
    public PageResult<ItemSummaryVO> list(int page, int size, Long categoryId, String keyword,
                                          BigDecimal minPrice, BigDecimal maxPrice,
                                          Integer conditionLevel, String sort, Integer status) {
        validatePage(page, size);
        int queryStatus = status != null ? status : STATUS_ON_SALE;
        LambdaQueryWrapper<IdleItem> wrapper = new LambdaQueryWrapper<IdleItem>()
                .eq(IdleItem::getStatus, queryStatus);
        if (categoryId != null) {
            wrapper.eq(IdleItem::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(IdleItem::getTitle, keyword).or().like(IdleItem::getSummary, keyword));
        }
        if (minPrice != null) {
            wrapper.ge(IdleItem::getSalePrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(IdleItem::getSalePrice, maxPrice);
        }
        if (conditionLevel != null) {
            wrapper.eq(IdleItem::getConditionLevel, conditionLevel);
        }
        applySort(wrapper, sort);
        Page<IdleItem> result = idleItemMapper.selectPage(new Page<>(page, size), wrapper);
        Map<Long, String> categoryNames = loadCategoryNames(result.getRecords());
        List<ItemSummaryVO> list = result.getRecords().stream()
                .map(item -> toSummary(item, categoryNames.get(item.getCategoryId()), findCoverUrl(item.getId())))
                .toList();
        return new PageResult<>(list, page, size, result.getTotal());
    }

    @Transactional(readOnly = true)
    public PageResult<ItemSummaryVO> searchInternal(cn.ynu.campus.relife.item.dto.InternalItemSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 10;
        String sort = request.getSort() != null ? request.getSort() : "latest";
        // 多关键词用 OR（任一命中标题/简介即可），避免 join 成「低于 生活用品」导致整段 LIKE 失败
        return listByKeywords(page, size, request.getCategoryId(), request.getKeywords(),
                request.getMinPrice(), request.getMaxPrice(), sort, STATUS_ON_SALE);
    }

    @Transactional(readOnly = true)
    public PageResult<ItemSummaryVO> listByKeywords(int page, int size, Long categoryId, List<String> keywords,
                                                    BigDecimal minPrice, BigDecimal maxPrice,
                                                    String sort, Integer status) {
        validatePage(page, size);
        int queryStatus = status != null ? status : STATUS_ON_SALE;
        LambdaQueryWrapper<IdleItem> wrapper = new LambdaQueryWrapper<IdleItem>()
                .eq(IdleItem::getStatus, queryStatus);
        if (categoryId != null) {
            wrapper.eq(IdleItem::getCategoryId, categoryId);
        }
        if (keywords != null && !keywords.isEmpty()) {
            List<String> usable = keywords.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .filter(k -> k.length() >= 2)
                    .distinct()
                    .limit(5)
                    .toList();
            if (!usable.isEmpty()) {
                wrapper.and(w -> {
                    for (int i = 0; i < usable.size(); i++) {
                        String kw = usable.get(i);
                        if (i == 0) {
                            w.like(IdleItem::getTitle, kw).or().like(IdleItem::getSummary, kw);
                        } else {
                            w.or().like(IdleItem::getTitle, kw).or().like(IdleItem::getSummary, kw);
                        }
                    }
                });
            }
        }
        if (minPrice != null) {
            wrapper.ge(IdleItem::getSalePrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(IdleItem::getSalePrice, maxPrice);
        }
        applySort(wrapper, sort);
        Page<IdleItem> result = idleItemMapper.selectPage(new Page<>(page, size), wrapper);
        Map<Long, String> categoryNames = loadCategoryNames(result.getRecords());
        List<ItemSummaryVO> list = result.getRecords().stream()
                .map(item -> toSummary(item, categoryNames.get(item.getCategoryId()), findCoverUrl(item.getId())))
                .toList();
        return new PageResult<>(list, page, size, result.getTotal());
    }

    @Transactional(readOnly = true)
    public PageResult<ItemSummaryVO> listMine(Long sellerId, int page, int size, Integer status) {
        validatePage(page, size);
        LambdaQueryWrapper<IdleItem> wrapper = new LambdaQueryWrapper<IdleItem>()
                .eq(IdleItem::getSellerId, sellerId);
        if (status != null) {
            wrapper.eq(IdleItem::getStatus, status);
        }
        wrapper.orderByDesc(IdleItem::getUpdatedAt);
        Page<IdleItem> result = idleItemMapper.selectPage(new Page<>(page, size), wrapper);
        Map<Long, String> categoryNames = loadCategoryNames(result.getRecords());
        List<ItemSummaryVO> list = result.getRecords().stream()
                .map(item -> toSummary(item, categoryNames.get(item.getCategoryId()), findCoverUrl(item.getId())))
                .toList();
        return new PageResult<>(list, page, size, result.getTotal());
    }

    @Transactional(readOnly = true)
    public ItemDetailVO getDetail(Long itemId) {
        ItemDetailVO cached = itemCacheService.getDetail(itemId);
        if (cached != null) {
            return cached;
        }
        ItemDetailVO detail = loadDetail(itemId);
        itemCacheService.putDetail(itemId, detail);
        return detail;
    }

    @Transactional
    public ItemDetailVO getDetailAndIncrView(Long itemId) {
        ItemDetailVO detail = getDetail(itemId);
        IdleItem item = idleItemMapper.selectById(itemId);
        if (item != null) {
            item.setViewCount(item.getViewCount() != null ? item.getViewCount() + 1 : 1);
            idleItemMapper.updateById(item);
            detail.setViewCount(item.getViewCount());
            itemCacheService.evictDetail(itemId);
        }
        return detail;
    }

    private ItemDetailVO loadDetail(Long itemId) {
        IdleItem item = idleItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        ItemCategory category = itemCategoryMapper.selectById(item.getCategoryId());
        String categoryName = category != null ? category.getName() : "";
        String sellerNickname = loadSellerNickname(item.getSellerId());
        List<ItemImage> images = itemImageMapper.selectList(new LambdaQueryWrapper<ItemImage>()
                .eq(ItemImage::getItemId, itemId)
                .orderByAsc(ItemImage::getSortOrder));
        List<ItemImageVO> imageVos = images.stream()
                .map(img -> new ItemImageVO(img.getImageUrl(), img.getIsCover() != null && img.getIsCover() == 1,
                        img.getSortOrder()))
                .toList();
        ItemDetailVO vo = new ItemDetailVO();
        vo.setItemId(item.getId());
        vo.setSellerId(item.getSellerId());
        vo.setSellerNickname(sellerNickname);
        vo.setCategoryId(item.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setTitle(item.getTitle());
        vo.setSummary(item.getSummary());
        vo.setDescription(item.getDescription());
        vo.setConditionLevel(item.getConditionLevel());
        vo.setOriginalPrice(item.getOriginalPrice());
        vo.setSalePrice(item.getSalePrice());
        vo.setStatus(item.getStatus());
        vo.setViewCount(item.getViewCount());
        vo.setImages(imageVos);
        vo.setAvailableQty(loadAvailableQty(itemId));
        vo.setPublishedAt(item.getPublishedAt());
        vo.setCreatedAt(item.getCreatedAt());
        return vo;
    }

    private Integer loadAvailableQty(Long itemId) {
        try {
            ApiResponse<FeignStockAvailableVO> response = stockFeignClient.available(itemId);
            if (response != null && response.getCode() == 0 && response.getData() != null) {
                return response.getData().getAvailableQty();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private String loadSellerNickname(Long sellerId) {
        try {
            ApiResponse<Map<String, FeignUserBriefVO>> response =
                    userFeignClient.batch(new FeignUserBatchRequest(List.of(sellerId)));
            if (response != null && response.getCode() == 0 && response.getData() != null) {
                FeignUserBriefVO brief = response.getData().get(String.valueOf(sellerId));
                if (brief != null) {
                    return brief.getNickname();
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private Map<Long, String> loadCategoryNames(List<IdleItem> items) {
        Map<Long, String> names = new HashMap<>();
        for (IdleItem item : items) {
            if (item.getCategoryId() == null || names.containsKey(item.getCategoryId())) {
                continue;
            }
            ItemCategory category = itemCategoryMapper.selectById(item.getCategoryId());
            names.put(item.getCategoryId(), category != null ? category.getName() : "");
        }
        return names;
    }

    private ItemSummaryVO toSummary(IdleItem item, String categoryName, String coverUrl) {
        ItemSummaryVO vo = new ItemSummaryVO();
        vo.setItemId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setSummary(item.getSummary());
        vo.setCoverUrl(coverUrl);
        vo.setSalePrice(item.getSalePrice());
        vo.setConditionLevel(item.getConditionLevel());
        vo.setCategoryId(item.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setSellerId(item.getSellerId());
        vo.setStatus(item.getStatus());
        vo.setPublishedAt(item.getPublishedAt());
        return vo;
    }

    private String findCoverUrl(Long itemId) {
        ItemImage cover = itemImageMapper.selectOne(new LambdaQueryWrapper<ItemImage>()
                .eq(ItemImage::getItemId, itemId)
                .eq(ItemImage::getIsCover, 1)
                .last("LIMIT 1"));
        if (cover != null) {
            return cover.getImageUrl();
        }
        ItemImage first = itemImageMapper.selectOne(new LambdaQueryWrapper<ItemImage>()
                .eq(ItemImage::getItemId, itemId)
                .orderByAsc(ItemImage::getSortOrder)
                .last("LIMIT 1"));
        return first != null ? first.getImageUrl() : "";
    }

    private void applySort(LambdaQueryWrapper<IdleItem> wrapper, String sort) {
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(IdleItem::getSalePrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(IdleItem::getSalePrice);
        } else {
            wrapper.orderByDesc(IdleItem::getPublishedAt);
        }
    }

    private void validatePage(int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.PAGE_INVALID);
        }
    }
}
