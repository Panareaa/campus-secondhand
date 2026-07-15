package cn.ynu.campus.relife.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "cn.ynu.campus.relife")
@EnableFeignClients
public class CampusItemServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusItemServiceApplication.class, args);
    }
}
