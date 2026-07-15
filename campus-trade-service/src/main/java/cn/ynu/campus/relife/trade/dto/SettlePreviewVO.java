package cn.ynu.campus.relife.trade.dto;

import java.math.BigDecimal;
import java.util.List;

public class SettlePreviewVO {

    private List<CartItemVO> items;
    private BigDecimal totalAmount;
    private String contactInfo;
    private Boolean contactReady;

    public List<CartItemVO> getItems() {
        return items;
    }

    public void setItems(List<CartItemVO> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Boolean getContactReady() {
        return contactReady;
    }

    public void setContactReady(Boolean contactReady) {
        this.contactReady = contactReady;
    }
}
