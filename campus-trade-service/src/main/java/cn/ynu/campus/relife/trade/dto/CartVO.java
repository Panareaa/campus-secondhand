package cn.ynu.campus.relife.trade.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartVO {

    private List<CartItemVO> items;
    private BigDecimal totalAmount;
    private Integer totalQuantity;

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

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
