package cn.ynu.campus.relife.item.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.item.domain.ItemCategory;
import cn.ynu.campus.relife.item.dto.CategoryVO;
import cn.ynu.campus.relife.item.mapper.ItemCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final ItemCategoryMapper itemCategoryMapper;
    private final ItemCacheService itemCacheService;

    public CategoryService(ItemCategoryMapper itemCategoryMapper, ItemCacheService itemCacheService) {
        this.itemCategoryMapper = itemCategoryMapper;
        this.itemCacheService = itemCacheService;
    }

    @Transactional(readOnly = true)
    public List<CategoryVO> listByParentId(Long parentId) {
        long pid = parentId != null ? parentId : 0L;
        List<CategoryVO> cached = itemCacheService.getCategories(pid);
        if (cached != null) {
            return cached;
        }
        List<ItemCategory> categories = itemCategoryMapper.selectList(new LambdaQueryWrapper<ItemCategory>()
                .eq(ItemCategory::getParentId, pid)
                .eq(ItemCategory::getStatus, 1)
                .orderByAsc(ItemCategory::getSortOrder));
        List<CategoryVO> result = categories.stream()
                .map(c -> new CategoryVO(c.getId(), c.getName(), c.getParentId(), c.getSortOrder()))
                .toList();
        itemCacheService.putCategories(pid, result);
        return result;
    }

    @Transactional(readOnly = true)
    public CategoryVO getById(Long categoryId) {
        ItemCategory category = itemCategoryMapper.selectById(categoryId);
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return new CategoryVO(category.getId(), category.getName(), category.getParentId(), category.getSortOrder());
    }
}
