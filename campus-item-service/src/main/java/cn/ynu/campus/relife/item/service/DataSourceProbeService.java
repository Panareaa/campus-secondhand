package cn.ynu.campus.relife.item.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DataSourceProbeService {

    private final DataSource masterDataSource;
    private final DataSource slaveDataSource;

    public DataSourceProbeService(@Qualifier("masterDataSource") DataSource masterDataSource,
                                  @Qualifier("slaveDataSource") DataSource slaveDataSource) {
        this.masterDataSource = masterDataSource;
        this.slaveDataSource = slaveDataSource;
    }

    public Map<String, Object> probe() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("master", probeOne(masterDataSource));
        result.put("slave", probeOne(slaveDataSource));
        result.put("readWriteSplit", true);
        return result;
    }

    private Map<String, String> probeOne(DataSource dataSource) {
        Map<String, String> info = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT @@hostname AS host, @@server_id AS server_id")) {
            if (rs.next()) {
                info.put("hostname", rs.getString("host"));
                info.put("serverId", rs.getString("server_id"));
            }
        } catch (Exception ex) {
            info.put("error", ex.getMessage());
        }
        return info;
    }
}
