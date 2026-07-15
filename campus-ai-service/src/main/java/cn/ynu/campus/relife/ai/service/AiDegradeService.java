package cn.ynu.campus.relife.ai.service;

import cn.ynu.campus.relife.ai.dto.DescribeRequest;
import cn.ynu.campus.relife.ai.dto.ParsedSearchCondition;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiDegradeService {

    /**
     * 与库表 item_category 一致：1教材书籍 2数码电子 3生活用品 4运动户外 5其他
     */
    private static final Map<String, Long> CATEGORY_ALIASES = new LinkedHashMap<>();

    static {
        CATEGORY_ALIASES.put("教材书籍", 1L);
        CATEGORY_ALIASES.put("教材", 1L);
        CATEGORY_ALIASES.put("书籍", 1L);
        CATEGORY_ALIASES.put("课本", 1L);
        CATEGORY_ALIASES.put("书", 1L);
        CATEGORY_ALIASES.put("数码电子", 2L);
        CATEGORY_ALIASES.put("数码", 2L);
        CATEGORY_ALIASES.put("电子", 2L);
        CATEGORY_ALIASES.put("手机", 2L);
        CATEGORY_ALIASES.put("电脑", 2L);
        CATEGORY_ALIASES.put("平板", 2L);
        CATEGORY_ALIASES.put("耳机", 2L);
        CATEGORY_ALIASES.put("生活用品", 3L);
        CATEGORY_ALIASES.put("日用品", 3L);
        CATEGORY_ALIASES.put("台灯", 3L);
        CATEGORY_ALIASES.put("收纳", 3L);
        CATEGORY_ALIASES.put("宿舍", 3L);
        CATEGORY_ALIASES.put("运动户外", 4L);
        CATEGORY_ALIASES.put("运动", 4L);
        CATEGORY_ALIASES.put("户外", 4L);
        CATEGORY_ALIASES.put("健身", 4L);
        CATEGORY_ALIASES.put("球拍", 4L);
        CATEGORY_ALIASES.put("其他", 5L);
    }

    private static final Pattern PRICE_LE = Pattern.compile(
            "(?:低于|不超过|少于|不到|小于)\\s*(\\d+(?:\\.\\d+)?)\\s*元?"
                    + "|(\\d+(?:\\.\\d+)?)\\s*元\\s*(?:以内|以下|内)"
                    + "|(\\d+(?:\\.\\d+)?)\\s*(?:以内|以下)"
                    + "|便宜.*?(\\d+(?:\\.\\d+)?)"
    );

    private static final Pattern PRICE_GE = Pattern.compile(
            "(?:高于|超过|大于|至少)\\s*(\\d+(?:\\.\\d+)?)\\s*元?"
                    + "|(\\d+(?:\\.\\d+)?)\\s*元\\s*(?:以上|起)"
    );

    private static final Pattern STOP_WORDS = Pattern.compile(
            "便宜|低价|高价|不超过|不低于|低于|高于|超过|少于|不到|小于|大于|至少"
                    + "|以内|以下|以上|左右|上下|元|块|的|一个|想|买|要|找|搜索|帮忙|推荐"
                    + "|校园|二手|闲置|物品|商品|东西"
    );

    public String buildDescribe(DescribeRequest request) {
        String category = StringUtils.hasText(request.getCategoryName()) ? request.getCategoryName() : "闲置好物";
        int level = request.getConditionLevel() != null ? request.getConditionLevel() : 3;
        String priceHint = request.getOriginalPrice() != null
                ? "原价约 ¥" + request.getOriginalPrice() + "，"
                : "";
        String keywordHint = "";
        if (request.getKeywords() != null && !request.getKeywords().isEmpty()) {
            keywordHint = "适合" + String.join("、", request.getKeywords()) + "场景，";
        }
        return String.format(
                "【%s】%s，%s成色 %d/5。%s物品保养良好，支持校园面交验货，价格可聊，有意者欢迎联系。",
                category, request.getTitle(), priceHint, level, keywordHint);
    }

    public List<String> suggestTags(DescribeRequest request) {
        List<String> tags = new ArrayList<>();
        if (StringUtils.hasText(request.getCategoryName())) {
            tags.add(request.getCategoryName());
        }
        if (request.getKeywords() != null) {
            tags.addAll(request.getKeywords());
        }
        tags.add("校园二手");
        if (request.getConditionLevel() != null && request.getConditionLevel() >= 4) {
            tags.add("九成新");
        }
        return tags.stream().distinct().limit(5).toList();
    }

    public ParsedSearchCondition parseSearchFallback(String query) {
        ParsedSearchCondition condition = new ParsedSearchCondition();
        String normalized = query == null ? "" : query.trim();
        Long categoryId = guessCategoryId(normalized);
        condition.setCategoryId(categoryId);
        condition.setMaxPrice(extractMaxPrice(normalized));
        condition.setMinPrice(extractMinPrice(normalized));
        condition.setKeywords(extractKeywords(normalized, categoryId));
        if (normalized.contains("便宜") || normalized.contains("低价")) {
            condition.setSort("price_asc");
        } else {
            condition.setSort("latest");
        }
        return condition;
    }

    private BigDecimal extractMaxPrice(String query) {
        Matcher matcher = PRICE_LE.matcher(query);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group != null && !group.isBlank()) {
                    return new BigDecimal(group);
                }
            }
        }
        if (query.contains("便宜") && !query.matches(".*\\d+.*")) {
            return BigDecimal.valueOf(50);
        }
        return null;
    }

    private BigDecimal extractMinPrice(String query) {
        Matcher matcher = PRICE_GE.matcher(query);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group != null && !group.isBlank()) {
                    return new BigDecimal(group);
                }
            }
        }
        return null;
    }

    private Long guessCategoryId(String query) {
        for (Map.Entry<String, Long> entry : CATEGORY_ALIASES.entrySet()) {
            if (query.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private List<String> extractKeywords(String query, Long categoryId) {
        String cleaned = query;
        if (categoryId != null) {
            for (Map.Entry<String, Long> entry : CATEGORY_ALIASES.entrySet()) {
                if (categoryId.equals(entry.getValue())) {
                    cleaned = cleaned.replace(entry.getKey(), " ");
                }
            }
        }
        cleaned = PRICE_LE.matcher(cleaned).replaceAll(" ");
        cleaned = PRICE_GE.matcher(cleaned).replaceAll(" ");
        cleaned = cleaned.replaceAll("\\d+(?:\\.\\d+)?\\s*元?", " ");
        cleaned = STOP_WORDS.matcher(cleaned).replaceAll(" ");
        cleaned = cleaned.replaceAll("[\\s,，、。！？；;]+", " ").trim();

        List<String> keywords = new ArrayList<>();
        for (String part : cleaned.split("\\s+")) {
            if (part.length() >= 2) {
                keywords.add(part);
            }
        }
        // 分类 + 价格已能约束时，不再把残留口语塞进关键词（避免 LIKE '%生活用品%' 把床上桌筛掉）
        if (keywords.isEmpty()) {
            return List.of();
        }
        return keywords.stream().distinct().limit(5).toList();
    }
}
