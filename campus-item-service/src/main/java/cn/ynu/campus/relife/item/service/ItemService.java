package cn.ynu.campus.relife.item.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.item.client.StockFeignClient;
import cn.ynu.campus.relife.item.client.dto.FeignInitStockRequest;
import cn.ynu.campus.relife.item.domain.IdleItem;
import cn.ynu.campus.relife.item.domain.ItemImage;
import cn.ynu.campus.relife.item.dto.ItemImageDTO;
import cn.ynu.campus.relife.item.dto.ItemSaleCheckVO;
import cn.ynu.campus.relife.item.dto.ItemStatusVO;
import cn.ynu.campus.relife.item.dto.PublishItemRequest;
import cn.ynu.campus.relife.item.mapper.IdleItemMapper;
import cn.ynu.campus.relife.item.mapper.ItemImageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_ON_SALE = 1;
    private static final int STATUS_OFF_SHELF = 3;

    private final IdleItemMapper idleItemMapper;
    private final ItemImageMapper itemImageMapper;
    private final StockFeignClient stockFeignClient;
    private final ItemCacheService itemCacheService;

    public ItemService(IdleItemMapper idleItemMapper, ItemImageMapper itemImageMapper,
                       StockFeignClient stockFeignClient, ItemCacheService itemCacheService) {
        this.idleItemMapper = idleItemMapper;
        this.itemImageMapper = itemImageMapper;
        this.stockFeignClient = stockFeignClient;
        this.itemCacheService = itemCacheService;
    }

    @Transactional
    public ItemStatusVO publish(Long sellerId, PublishItemRequest request) {
        IdleItem item = new IdleItem();
        item.setSellerId(sellerId);
        item.setCategoryId(request.getCategoryId());
        item.setTitle(request.getTitle());
        item.setSummary(request.getSummary() != null ? request.getSummary() : "");
        item.setDescription(request.getDescription());
        item.setConditionLevel(request.getConditionLevel() != null ? request.getConditionLevel() : 3);
        item.setOriginalPrice(request.getOriginalPrice());
        item.setSalePrice(request.getSalePrice());
        item.setViewCount(0);
        boolean shouldPublish = Boolean.TRUE.equals(request.getPublish());
        item.setStatus(shouldPublish ? STATUS_ON_SALE : STATUS_DRAFT);
        if (shouldPublish) {
            item.setPublishedAt(LocalDateTime.now());
        }
        idleItemMapper.insert(item);
        saveImages(item.getId(), request.getImages());
        if (shouldPublish) {
            initStock(item.getId());
        }
        itemCacheService.evictDetail(item.getId());
        return new ItemStatusVO(item.getId(), item.getStatus());
    }

    @Transactional
    public ItemStatusVO publishItem(Long sellerId, Long itemId) {
        IdleItem item = requireOwnedItem(sellerId, itemId);
        if (item.getStatus() != null && item.getStatus() == STATUS_ON_SALE) {
            return new ItemStatusVO(itemId, item.getStatus());
        }
        item.setStatus(STATUS_ON_SALE);
        item.setPublishedAt(LocalDateTime.now());
        idleItemMapper.updateById(item);
        initStock(itemId);
        itemCacheService.evictDetail(itemId);
        return new ItemStatusVO(itemId, STATUS_ON_SALE);
    }

    @Transactional
    public ItemStatusVO offShelf(Long sellerId, Long itemId) {
        IdleItem item = requireOwnedItem(sellerId, itemId);
        item.setStatus(STATUS_OFF_SHELF);
        idleItemMapper.updateById(item);
        itemCacheService.evictDetail(itemId);
        return new ItemStatusVO(itemId, STATUS_OFF_SHELF);
    }

    public List<ItemSaleCheckVO> batchSaleCheck(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return List.of();
        }
        List<ItemSaleCheckVO> result = new ArrayList<>();
        for (Long itemId : itemIds) {
            result.add(saleCheck(itemId));
        }
        return result;
    }

    public ItemSaleCheckVO saleCheck(Long itemId) {
        IdleItem item = idleItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        ItemSaleCheckVO vo = new ItemSaleCheckVO();
        vo.setItemId(itemId);
        vo.setSellerId(item.getSellerId());
        vo.setTitle(item.getTitle());
        vo.setSalePrice(item.getSalePrice());
        vo.setStatus(item.getStatus());
        vo.setOnSale(item.getStatus() != null && item.getStatus() == STATUS_ON_SALE);
        vo.setCoverUrl(findCoverUrl(itemId));
        return vo;
    }

    private void saveImages(Long itemId, List<ItemImageDTO> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (ItemImageDTO dto : images) {
            ItemImage image = new ItemImage();
            image.setItemId(itemId);
            image.setImageUrl(dto.getImageUrl());
            image.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
            image.setIsCover(Boolean.TRUE.equals(dto.getIsCover()) ? 1 : 0);
            itemImageMapper.insert(image);
        }
    }

    private void initStock(Long itemId) {
        var response = stockFeignClient.initStock(new FeignInitStockRequest(itemId, 1));
        if (response == null || response.getCode() != 0) {
            throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
        }
    }

    private IdleItem requireOwnedItem(Long sellerId, Long itemId) {
        IdleItem item = idleItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (!sellerId.equals(item.getSellerId())) {
            throw new BusinessException(ErrorCode.ITEM_NOT_OWNER);
        }
        return item;
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
}
