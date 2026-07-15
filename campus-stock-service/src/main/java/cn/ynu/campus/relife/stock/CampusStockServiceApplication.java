package cn.ynu.campus.relife.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cn.ynu.campus.relife")
public class CampusStockServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusStockServiceApplication.class, args);
    }
}
