package cn.ynu.campus.relife.stock.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("cn.ynu.campus.relife.stock.mapper")
public class StockServiceConfig {
}
