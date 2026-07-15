package cn.ynu.campus.relife.trade.config;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * ShardingSphere-JDBC：trade_order / trade_line 按 buyer_id % 2 分表
 */
@Configuration
public class ShardingSphereDataSourceConfig {

    @Bean
    @Primary
    public DataSource shardingDataSource(
            @Value("${MYSQL_HOST:127.0.0.1}") String host,
            @Value("${MYSQL_PORT:3306}") int port,
            @Value("${MYSQL_USER:relife}") String user,
            @Value("${MYSQL_PASSWORD:relife123}") String password) throws IOException, SQLException {
        String yaml = new ClassPathResource("sharding.yaml").getContentAsString(StandardCharsets.UTF_8)
                .replace("PLACEHOLDER_HOST", host)
                .replace("PLACEHOLDER_PORT", String.valueOf(port))
                .replace("PLACEHOLDER_USER", user)
                .replace("PLACEHOLDER_PASSWORD", password);
        return YamlShardingSphereDataSourceFactory.createDataSource(yaml.getBytes(StandardCharsets.UTF_8));
    }
}
