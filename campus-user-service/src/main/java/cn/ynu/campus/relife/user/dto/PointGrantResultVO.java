package cn.ynu.campus.relife.user.dto;

public class PointGrantResultVO {

    private Long ledgerId;
    private Integer balanceAfter;
    private Boolean duplicate;

    public PointGrantResultVO() {
    }

    public PointGrantResultVO(Long ledgerId, Integer balanceAfter, Boolean duplicate) {
        this.ledgerId = ledgerId;
        this.balanceAfter = balanceAfter;
        this.duplicate = duplicate;
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Boolean getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
    }
}
