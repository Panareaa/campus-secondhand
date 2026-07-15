package cn.ynu.campus.relife.gateway.filter;

import cn.ynu.campus.relife.common.core.constant.GatewayHeaders;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.gateway.security.GatewayJwtParser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * JWT 鉴权：白名单放行，其余校验 Bearer Token 并透传 X-User-Id
 */
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final GatewayJwtParser jwtParser;

    public JwtAuthGlobalFilter(GatewayJwtParser jwtParser) {
        this.jwtParser = jwtParser;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getURI().getRawPath();

        if (isWhitelisted(method, path)) {
            return chain.filter(exchange);
        }

        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        String token = auth.substring(7);
        try {
            String userId = jwtParser.parse(token).getSubject();
            ServerHttpRequest mutated = request.mutate()
                    .header(GatewayHeaders.USER_ID, userId)
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (ExpiredJwtException ex) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException ex) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, ErrorCode.TOKEN_INVALID);
        }
    }

    private boolean isWhitelisted(String method, String path) {
        if ("GET".equals(method) && path.startsWith("/actuator/health")) {
            return true;
        }
        if ("POST".equals(method) && ("/api/auth/register".equals(path) || "/api/auth/login".equals(path))) {
            return true;
        }
        if ("POST".equals(method) && "/api/ai/search".equals(path)) {
            return true;
        }
        if ("GET".equals(method)) {
            if ("/api/categories".equals(path) || path.matches("/api/categories/\\d+")) {
                return true;
            }
            if ("/api/items".equals(path) || path.matches("/api/items/\\d+")) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, ErrorCode errorCode) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":" + errorCode.getCode()
                + ",\"message\":\"" + errorCode.getMessage()
                + "\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
