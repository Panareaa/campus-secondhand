package cn.ynu.campus.relife.trade.dto;

import java.math.BigDecimal;

public class CheckoutLineVO {

    private Long itemId;
    private String itemTitle;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineAmount;

    public CheckoutLineVO() {
    }

    public CheckoutLineVO(Long itemId, String itemTitle, BigDecimal unitPrice, Integer quantity, BigDecimal lineAmount) {
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineAmount = lineAmount;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLineAmount() {
        return lineAmount;
    }

    public void setLineAmount(BigDecimal lineAmount) {
        this.lineAmount = lineAmount;
    }
}
