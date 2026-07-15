package cn.ynu.campus.relife.user.dto;

public class UserBriefVO {

    private String nickname;
    private Integer reputation;

    public UserBriefVO() {
    }

    public UserBriefVO(String nickname, Integer reputation) {
        this.nickname = nickname;
        this.reputation = reputation;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getReputation() {
        return reputation;
    }

    public void setReputation(Integer reputation) {
        this.reputation = reputation;
    }
}
