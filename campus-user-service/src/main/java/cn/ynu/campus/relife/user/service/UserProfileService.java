package cn.ynu.campus.relife.user.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.user.domain.Account;
import cn.ynu.campus.relife.user.domain.PointAccount;
import cn.ynu.campus.relife.user.dto.CertificationRequest;
import cn.ynu.campus.relife.user.dto.CertificationVO;
import cn.ynu.campus.relife.user.dto.UpdateContactRequest;
import cn.ynu.campus.relife.user.dto.UpdateProfileRequest;
import cn.ynu.campus.relife.user.dto.UserProfileVO;
import cn.ynu.campus.relife.user.mapper.AccountMapper;
import cn.ynu.campus.relife.user.mapper.PointAccountMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final AccountMapper accountMapper;
    private final PointAccountMapper pointAccountMapper;

    public UserProfileService(AccountMapper accountMapper, PointAccountMapper pointAccountMapper) {
        this.accountMapper = accountMapper;
        this.pointAccountMapper = pointAccountMapper;
    }

    public UserProfileVO getProfile(Long userId) {
        Account account = requireAccount(userId);
        return toProfileVO(account);
    }

    @Transactional
    public UserProfileVO updateProfile(Long userId, UpdateProfileRequest request) {
        Account account = requireAccount(userId);
        if (request.getNickname() != null) {
            account.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            account.setAvatarUrl(request.getAvatarUrl());
        }
        accountMapper.updateById(account);
        return toProfileVO(account);
    }

    @Transactional
    public UserProfileVO updateContact(Long userId, UpdateContactRequest request) {
        Account account = requireAccount(userId);
        account.setContactInfo(request.getContactInfo());
        accountMapper.updateById(account);
        return toProfileVO(account);
    }

    @Transactional
    public CertificationVO certify(Long userId, CertificationRequest request) {
        Account account = requireAccount(userId);
        account.setCertStatus(1);
        accountMapper.updateById(account);
        return new CertificationVO(1);
    }

    private Account requireAccount(Long userId) {
        Account account = accountMapper.selectById(userId);
        if (account == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return account;
    }

    private UserProfileVO toProfileVO(Account account) {
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(account.getId());
        vo.setCampusId(account.getCampusId());
        vo.setLoginName(account.getLoginName());
        vo.setNickname(account.getNickname());
        vo.setContactInfo(account.getContactInfo());
        vo.setAvatarUrl(account.getAvatarUrl());
        vo.setRole(account.getRole() != null && account.getRole() == 1 ? "ADMIN" : "USER");
        vo.setCertStatus(account.getCertStatus());
        vo.setReputation(account.getReputation());
        vo.setCreatedAt(account.getCreatedAt());
        PointAccount point = pointAccountMapper.selectOne(new LambdaQueryWrapper<PointAccount>()
                .eq(PointAccount::getAccountId, account.getId()));
        vo.setGreenPoints(point != null ? point.getBalance() : 0);
        return vo;
    }
}
