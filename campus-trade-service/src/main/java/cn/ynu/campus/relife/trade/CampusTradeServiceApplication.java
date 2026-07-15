package cn.ynu.campus.relife.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import cn.ynu.campus.relife.trade.config.TradeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "cn.ynu.campus.relife")
@EnableFeignClients
@EnableScheduling
@EnableConfigurationProperties(TradeProperties.class)
public class CampusTradeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusTradeServiceApplication.class, args);
    }
}
