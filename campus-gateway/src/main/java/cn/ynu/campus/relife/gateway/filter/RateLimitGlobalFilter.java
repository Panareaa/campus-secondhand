package cn.ynu.campus.relife.gateway.filter;

import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.gateway.config.RateLimitProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易网关限流：滑动窗口计数，超限返回 42900
 */
@Component
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();

    public RateLimitGlobalFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!properties.isEnabled()) {
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getURI().getRawPath();
        if (!properties.getPaths().contains(path)) {
            return chain.filter(exchange);
        }
        String clientKey = resolveClientKey(exchange.getRequest()) + ":" + path;
        if (!tryAcquire(clientKey)) {
            return writeError(exchange);
        }
        return chain.filter(exchange);
    }

    private boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        long windowMs = properties.getWindowSeconds() * 1000L;
        Deque<Long> deque = windows.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && now - deque.peekFirst() > windowMs) {
                deque.pollFirst();
            }
            if (deque.size() >= properties.getLimit()) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    private String resolveClientKey(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    private Mono<Void> writeError(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":" + ErrorCode.RATE_LIMITED.getCode()
                + ",\"message\":\"" + ErrorCode.RATE_LIMITED.getMessage()
                + "\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}";
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
