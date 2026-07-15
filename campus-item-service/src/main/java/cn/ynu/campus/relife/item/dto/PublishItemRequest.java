package cn.ynu.campus.relife.item.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class PublishItemRequest {

    @NotNull
    private Long categoryId;

    @NotBlank
    @Size(max = 128)
    private String title;

    @Size(max = 512)
    private String summary;

    private String description;

    @Min(1)
    @Max(5)
    private Integer conditionLevel = 3;

    @DecimalMin("0.00")
    private BigDecimal originalPrice = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal salePrice;

    private List<ItemImageDTO> images;

    private Boolean publish = false;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getConditionLevel() {
        return conditionLevel;
    }

    public void setConditionLevel(Integer conditionLevel) {
        this.conditionLevel = conditionLevel;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public List<ItemImageDTO> getImages() {
        return images;
    }

    public void setImages(List<ItemImageDTO> images) {
        this.images = images;
    }

    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }
}
