package cn.ynu.campus.relife.item.dto;

public class CategoryVO {

    private Long categoryId;
    private String name;
    private Long parentId;
    private Integer sortOrder;

    public CategoryVO() {
    }

    public CategoryVO(Long categoryId, String name, Long parentId, Integer sortOrder) {
        this.categoryId = categoryId;
        this.name = name;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
