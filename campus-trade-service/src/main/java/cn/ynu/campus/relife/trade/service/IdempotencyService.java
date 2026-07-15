package cn.ynu.campus.relife.trade.service;

import cn.ynu.campus.relife.trade.dto.CheckoutVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

@Service
public class IdempotencyService {

    private static final Duration TTL = Duration.ofHours(24);
    private static final String PREFIX = "idempotency:checkout:";

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    public IdempotencyService(StringRedisTemplate redis, JsonMapper jsonMapper) {
        this.redis = redis;
        this.jsonMapper = jsonMapper;
    }

    public CheckoutVO getCached(Long buyerId, String idempotencyKey) {
        String json = redis.opsForValue().get(key(buyerId, idempotencyKey));
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return jsonMapper.readValue(json, CheckoutVO.class);
        } catch (Exception ex) {
            redis.delete(key(buyerId, idempotencyKey));
            return null;
        }
    }

    public void cache(Long buyerId, String idempotencyKey, CheckoutVO result) {
        try {
            redis.opsForValue().set(key(buyerId, idempotencyKey), jsonMapper.writeValueAsString(result), TTL);
        } catch (Exception ignored) {
        }
    }

    private String key(Long buyerId, String idempotencyKey) {
        return PREFIX + buyerId + ":" + idempotencyKey;
    }
}
