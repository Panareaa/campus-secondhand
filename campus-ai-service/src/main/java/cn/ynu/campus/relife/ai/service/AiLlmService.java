package cn.ynu.campus.relife.ai.service;

import cn.ynu.campus.relife.ai.config.AiProperties;
import cn.ynu.campus.relife.ai.dto.DescribeRequest;
import cn.ynu.campus.relife.ai.dto.ParsedSearchCondition;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 等价 Spring AI ChatClient：Boot 4 下 spring-ai-starter 暂不可用，使用 RestClient 直调 OpenAI Chat Completions。
 */
@Service
public class AiLlmService {

    private static final String SEARCH_SYSTEM = """
            你是校园二手搜索助手。将用户自然语言搜索解析为 JSON，仅输出 JSON，不要 markdown。
            字段：categoryId(可为null), keywords(字符串数组), minPrice, maxPrice, sort(latest|price_asc|price_desc)。
            categoryId 参考（与系统分类一致）：1教材书籍 2数码电子 3生活用品 4运动户外 5其他。
            若用户只给分类+价格区间，keywords 应为空数组；不要把「生活用品」「低于」等词放进 keywords。
            """;

    private static final String DESCRIBE_SYSTEM = """
            你是校园二手物品描述助手。根据用户提供的信息写一段 80~150 字的出售描述，语气真诚简洁。
            只输出描述正文，不要标题和 markdown。
            """;

    private final RestClient restClient;
    private final AiProperties aiProperties;
    private final JsonMapper jsonMapper;

    public AiLlmService(AiProperties aiProperties, JsonMapper jsonMapper) {
        this.aiProperties = aiProperties;
        this.jsonMapper = jsonMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + System.getenv().getOrDefault("OPENAI_API_KEY", "sk-dev-placeholder"))
                .build();
    }

    public Optional<String> generateDescribe(DescribeRequest request) {
        return chat(DESCRIBE_SYSTEM, buildDescribePrompt(request));
    }

    public Optional<ParsedSearchCondition> parseSearchQuery(String query) {
        Optional<String> content = chat(SEARCH_SYSTEM, query);
        if (content.isEmpty()) {
            return Optional.empty();
        }
        try {
            ParsedSearchCondition condition = jsonMapper.readValue(extractJson(content.get()), ParsedSearchCondition.class);
            return Optional.of(condition);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Optional<String> chat(String system, String user) {
        if (!aiProperties.isLlmEnabled()) {
            return Optional.empty();
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", aiProperties.getModelName(),
                    "messages", List.of(
                            Map.of("role", "system", "content", system),
                            Map.of("role", "user", "content", user)
                    ),
                    "temperature", 0.7
            );
            String response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            if (!StringUtils.hasText(response)) {
                return Optional.empty();
            }
            JsonNode root = jsonMapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            return content.isMissingNode() || !StringUtils.hasText(content.asText())
                    ? Optional.empty()
                    : Optional.of(content.asText().trim());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String buildDescribePrompt(DescribeRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("标题：").append(request.getTitle()).append('\n');
        if (StringUtils.hasText(request.getCategoryName())) {
            sb.append("分类：").append(request.getCategoryName()).append('\n');
        }
        sb.append("成色：").append(request.getConditionLevel()).append("/5\n");
        if (request.getOriginalPrice() != null) {
            sb.append("原价：").append(request.getOriginalPrice()).append('\n');
        }
        if (request.getKeywords() != null && !request.getKeywords().isEmpty()) {
            sb.append("关键词：").append(String.join("、", request.getKeywords())).append('\n');
        }
        return sb.toString();
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
