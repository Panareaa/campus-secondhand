package cn.ynu.campus.relife.gateway;

import cn.ynu.campus.relife.gateway.config.GatewayJwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GatewayJwtProperties.class)
public class CampusGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusGatewayApplication.class, args);
    }
}
