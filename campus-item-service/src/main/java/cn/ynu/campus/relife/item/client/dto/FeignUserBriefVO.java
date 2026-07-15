package cn.ynu.campus.relife.item.client.dto;

public class FeignUserBriefVO {

    private String nickname;
    private Integer reputation;

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
