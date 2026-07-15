package cn.ynu.campus.relife.trade.client.dto;

public class FeignPointGrantRequest {

    private Long accountId;
    private Long tradeId;
    private Integer changeAmount;
    private String ruleCode;
    private String idempotentKey;
    private String remark;

    public FeignPointGrantRequest() {
    }

    public FeignPointGrantRequest(Long accountId, Long tradeId, Integer changeAmount,
                                  String ruleCode, String idempotentKey, String remark) {
        this.accountId = accountId;
        this.tradeId = tradeId;
        this.changeAmount = changeAmount;
        this.ruleCode = ruleCode;
        this.idempotentKey = idempotentKey;
        this.remark = remark;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Integer changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getIdempotentKey() {
        return idempotentKey;
    }

    public void setIdempotentKey(String idempotentKey) {
        this.idempotentKey = idempotentKey;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
