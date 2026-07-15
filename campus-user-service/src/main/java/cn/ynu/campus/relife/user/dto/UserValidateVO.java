package cn.ynu.campus.relife.user.dto;

public class UserValidateVO {

    private Boolean valid;
    private Integer status;
    private String contactInfo;
    private Boolean contactReady;

    public UserValidateVO() {
    }

    public UserValidateVO(Boolean valid, Integer status, String contactInfo, Boolean contactReady) {
        this.valid = valid;
        this.status = status;
        this.contactInfo = contactInfo;
        this.contactReady = contactReady;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
