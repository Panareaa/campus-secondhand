package cn.ynu.campus.relife.common.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "campus.internal")
public class InternalAuthProperties {

    private String token = "CampusRelifeInternalDevToken2026";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
