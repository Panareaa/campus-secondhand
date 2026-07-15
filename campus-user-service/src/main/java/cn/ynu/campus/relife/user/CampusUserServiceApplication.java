package cn.ynu.campus.relife.user;

import cn.ynu.campus.relife.user.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "cn.ynu.campus.relife")
@EnableConfigurationProperties(JwtProperties.class)
@EnableFeignClients
public class CampusUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusUserServiceApplication.class, args);
    }
}
