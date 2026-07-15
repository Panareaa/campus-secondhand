package cn.ynu.campus.relife.ai;

import cn.ynu.campus.relife.ai.config.AiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "cn.ynu.campus.relife")
@EnableFeignClients
@EnableConfigurationProperties(AiProperties.class)
public class CampusAiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusAiServiceApplication.class, args);
    }
}
