package cn.ynu.campus.relife.item.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource masterDataSource(
            @Value("${spring.datasource.master.url}") String url,
            @Value("${spring.datasource.master.username}") String username,
            @Value("${spring.datasource.master.password}") String password,
            @Value("${spring.datasource.master.driver-class-name}") String driverClassName) {
        return buildDataSource(url, username, password, driverClassName);
    }

    @Bean
    public DataSource slaveDataSource(
            @Value("${spring.datasource.slave.url}") String url,
            @Value("${spring.datasource.slave.username}") String username,
            @Value("${spring.datasource.slave.password}") String password,
            @Value("${spring.datasource.slave.driver-class-name}") String driverClassName) {
        return buildDataSource(url, username, password, driverClassName);
    }

    @Bean
    @Primary
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource) {
        ReadWriteRoutingDataSource routing = new ReadWriteRoutingDataSource();
        Map<Object, Object> targets = new HashMap<>();
        targets.put("master", masterDataSource);
        targets.put("slave", slaveDataSource);
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(masterDataSource);
        routing.afterPropertiesSet();
        return routing;
    }

    private HikariDataSource buildDataSource(String url, String username, String password, String driverClassName) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(10);
        return dataSource;
    }
}
