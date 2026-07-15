package cn.ynu.campus.relife.user.dto;

import java.time.LocalDateTime;

public class PointBalanceVO {

    private Integer balance;
    private LocalDateTime updatedAt;

    public PointBalanceVO() {
    }

    public PointBalanceVO(Integer balance, LocalDateTime updatedAt) {
        this.balance = balance;
        this.updatedAt = updatedAt;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
