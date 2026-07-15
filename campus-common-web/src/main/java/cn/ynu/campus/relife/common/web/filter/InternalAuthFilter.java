package cn.ynu.campus.relife.common.web.filter;

import cn.ynu.campus.relife.common.core.constant.GatewayHeaders;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.web.config.InternalAuthProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 校验 /internal/** 请求的 X-Internal-Token
 */
public class InternalAuthFilter extends OncePerRequestFilter {

    private final InternalAuthProperties properties;

    public InternalAuthFilter(InternalAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader(GatewayHeaders.INTERNAL_TOKEN);
        if (token == null || !token.equals(properties.getToken())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String body = "{\"code\":" + ErrorCode.FORBIDDEN.getCode()
                    + ",\"message\":\"内部接口鉴权失败\",\"data\":null}";
            response.getWriter().write(body);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
