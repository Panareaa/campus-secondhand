package cn.ynu.campus.relife.user.dto;

public class CertificationVO {

    private Integer certStatus;

    public CertificationVO() {
    }

    public CertificationVO(Integer certStatus) {
        this.certStatus = certStatus;
    }

    public Integer getCertStatus() {
        return certStatus;
    }

    public void setCertStatus(Integer certStatus) {
        this.certStatus = certStatus;
    }
}
