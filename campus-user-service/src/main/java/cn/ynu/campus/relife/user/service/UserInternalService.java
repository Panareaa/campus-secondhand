package cn.ynu.campus.relife.user.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.user.domain.Account;
import cn.ynu.campus.relife.user.dto.UserBriefVO;
import cn.ynu.campus.relife.user.dto.UserValidateVO;
import cn.ynu.campus.relife.user.mapper.AccountMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInternalService {

    private final AccountMapper accountMapper;

    public UserInternalService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    public UserValidateVO validate(Long userId) {
        Account account = accountMapper.selectById(userId);
        if (account == null || account.getStatus() != null && account.getStatus() != 0) {
            return new UserValidateVO(false, account != null ? account.getStatus() : null, null, false);
        }
        String contact = account.getContactInfo() != null ? account.getContactInfo().trim() : "";
        boolean contactReady = !contact.isEmpty();
        return new UserValidateVO(true, account.getStatus(), contact, contactReady);
    }

    public Map<String, UserBriefVO> batchQuery(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<String, UserBriefVO> result = new HashMap<>();
        for (Long userId : userIds) {
            Account account = accountMapper.selectById(userId);
            if (account == null) {
                continue;
            }
            result.put(String.valueOf(userId),
                    new UserBriefVO(account.getNickname(), account.getReputation()));
        }
        return result;
    }

    public UserBriefVO requireUser(Long userId) {
        Account account = accountMapper.selectById(userId);
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return new UserBriefVO(account.getNickname(), account.getReputation());
    }
}
