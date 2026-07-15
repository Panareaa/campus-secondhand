package cn.ynu.campus.relife.item.service;

import cn.ynu.campus.relife.item.dto.CategoryVO;
import cn.ynu.campus.relife.item.dto.ItemDetailVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

@Service
public class ItemCacheService {

    private static final Duration DETAIL_TTL = Duration.ofMinutes(5);
    private static final Duration CATEGORY_TTL = Duration.ofMinutes(10);
    private static final String DETAIL_PREFIX = "item:detail:";
    private static final String CATEGORY_PREFIX = "item:categories:parent:";

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    public ItemCacheService(StringRedisTemplate redis, JsonMapper jsonMapper) {
        this.redis = redis;
        this.jsonMapper = jsonMapper;
    }

    public ItemDetailVO getDetail(Long itemId) {
        String json = redis.opsForValue().get(DETAIL_PREFIX + itemId);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return jsonMapper.readValue(json, ItemDetailVO.class);
        } catch (Exception ex) {
            redis.delete(DETAIL_PREFIX + itemId);
            return null;
        }
    }

    public void putDetail(Long itemId, ItemDetailVO detail) {
        try {
            redis.opsForValue().set(DETAIL_PREFIX + itemId, jsonMapper.writeValueAsString(detail), DETAIL_TTL);
        } catch (Exception ignored) {
            // 缓存失败不影响主流程
        }
    }

    public void evictDetail(Long itemId) {
        redis.delete(DETAIL_PREFIX + itemId);
    }

    @SuppressWarnings("null")
    public List<CategoryVO> getCategories(Long parentId) {
        String json = redis.opsForValue().get(CATEGORY_PREFIX + parentId);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            CategoryVO[] arr = jsonMapper.readValue(json, CategoryVO[].class);
            return List.of(arr);
        } catch (Exception ex) {
            redis.delete(CATEGORY_PREFIX + parentId);
            return null;
        }
    }

    public void putCategories(Long parentId, List<CategoryVO> categories) {
        try {
            redis.opsForValue().set(CATEGORY_PREFIX + parentId, jsonMapper.writeValueAsString(categories), CATEGORY_TTL);
        } catch (Exception ignored) {
        }
    }

    public void evictCategories() {
        redis.delete(redis.keys(CATEGORY_PREFIX + "*"));
    }
}
