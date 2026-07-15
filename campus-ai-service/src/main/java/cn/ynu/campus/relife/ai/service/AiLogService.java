package cn.ynu.campus.relife.ai.service;

import cn.ynu.campus.relife.ai.domain.AiPromptLog;
import cn.ynu.campus.relife.ai.domain.AiSearchLog;
import cn.ynu.campus.relife.ai.mapper.AiPromptLogMapper;
import cn.ynu.campus.relife.ai.mapper.AiSearchLogMapper;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class AiLogService {

    public static final int STATUS_FAIL = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_DEGRADED = 2;

    private final AiPromptLogMapper aiPromptLogMapper;
    private final AiSearchLogMapper aiSearchLogMapper;
    private final JsonMapper jsonMapper;

    public AiLogService(AiPromptLogMapper aiPromptLogMapper,
                        AiSearchLogMapper aiSearchLogMapper,
                        JsonMapper jsonMapper) {
        this.aiPromptLogMapper = aiPromptLogMapper;
        this.aiSearchLogMapper = aiSearchLogMapper;
        this.jsonMapper = jsonMapper;
    }

    public void logDescribe(Long accountId, Object input, Object output, String modelName, int latencyMs, int status) {
        AiPromptLog log = new AiPromptLog();
        log.setAccountId(accountId);
        log.setScene("DESCRIBE");
        log.setInputJson(toJson(input));
        log.setOutputJson(toJson(output));
        log.setModelName(modelName);
        log.setLatencyMs(latencyMs);
        log.setStatus(status);
        aiPromptLogMapper.insert(log);
    }

    public void logSearch(Long accountId, String rawQuery, Object parsed, int resultCount, int latencyMs) {
        AiSearchLog log = new AiSearchLog();
        log.setAccountId(accountId);
        log.setRawQuery(rawQuery);
        log.setParsedJson(toJson(parsed));
        log.setResultCount(resultCount);
        log.setLatencyMs(latencyMs);
        aiSearchLogMapper.insert(log);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "{}";
        }
    }
}
