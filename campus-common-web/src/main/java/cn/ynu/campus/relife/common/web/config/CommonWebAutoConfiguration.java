package cn.ynu.campus.relife.common.web.config;

import cn.ynu.campus.relife.common.web.feign.InternalFeignRequestInterceptor;
import cn.ynu.campus.relife.common.web.filter.InternalAuthFilter;
import cn.ynu.campus.relife.common.web.filter.TraceIdFilter;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(InternalAuthProperties.class)
public class CommonWebAutoConfiguration {

    @Bean
    public FilterRegistrationBean<InternalAuthFilter> internalAuthFilter(InternalAuthProperties properties) {
        FilterRegistrationBean<InternalAuthFilter> bean = new FilterRegistrationBean<>(
                new InternalAuthFilter(properties));
        bean.setOrder(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> bean = new FilterRegistrationBean<>(new TraceIdFilter());
        bean.setOrder(0);
        return bean;
    }

    @Bean
    @ConditionalOnClass(RequestInterceptor.class)
    public RequestInterceptor internalFeignRequestInterceptor(InternalAuthProperties properties) {
        return new InternalFeignRequestInterceptor(properties);
    }
}
