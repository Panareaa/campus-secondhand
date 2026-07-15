package cn.ynu.campus.relife.common.web.feign;

import cn.ynu.campus.relife.common.core.constant.GatewayHeaders;
import cn.ynu.campus.relife.common.web.config.InternalAuthProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Feign 出站：透传 TraceId + 内部 Token
 */
public class InternalFeignRequestInterceptor implements RequestInterceptor {

    private final InternalAuthProperties properties;

    public InternalFeignRequestInterceptor(InternalAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(GatewayHeaders.INTERNAL_TOKEN, properties.getToken());
        String traceId = MDC.get(GatewayHeaders.TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        template.header(GatewayHeaders.TRACE_ID, traceId);
    }
}
