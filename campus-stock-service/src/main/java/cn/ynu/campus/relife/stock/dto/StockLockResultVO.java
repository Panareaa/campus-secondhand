package cn.ynu.campus.relife.stock.dto;

public class StockLockResultVO {

    private Long lockLogId;
    private Boolean success;
    private Boolean duplicate;

    public StockLockResultVO() {
    }

    public StockLockResultVO(Long lockLogId, Boolean success, Boolean duplicate) {
        this.lockLogId = lockLogId;
        this.success = success;
        this.duplicate = duplicate;
    }

    public Long getLockLogId() {
        return lockLogId;
    }

    public void setLockLogId(Long lockLogId) {
        this.lockLogId = lockLogId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Boolean getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
    }
}
