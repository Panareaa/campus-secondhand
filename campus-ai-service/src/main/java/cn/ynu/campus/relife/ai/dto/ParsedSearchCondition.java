package cn.ynu.campus.relife.ai.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ParsedSearchCondition {

    private Long categoryId;
    private List<String> keywords = new ArrayList<>();
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort = "latest";

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
