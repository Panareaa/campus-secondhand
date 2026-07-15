package cn.ynu.campus.relife.user.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.user.domain.Account;
import cn.ynu.campus.relife.user.domain.PointAccount;
import cn.ynu.campus.relife.user.dto.AuthTokenResponse;
import cn.ynu.campus.relife.user.dto.LoginRequest;
import cn.ynu.campus.relife.user.dto.RegisterRequest;
import cn.ynu.campus.relife.user.mapper.AccountMapper;
import cn.ynu.campus.relife.user.mapper.PointAccountMapper;
import cn.ynu.campus.relife.user.security.JwtTokenProvider;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AccountMapper accountMapper;
    private final PointAccountMapper pointAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(AccountMapper accountMapper,
                       PointAccountMapper pointAccountMapper,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.accountMapper = accountMapper;
        this.pointAccountMapper = pointAccountMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        if (existsByLoginName(request.getLoginName())) {
            throw new BusinessException(ErrorCode.LOGIN_NAME_EXISTS);
        }
        if (existsByCampusId(request.getCampusId())) {
            throw new BusinessException(ErrorCode.CAMPUS_ID_EXISTS);
        }

        Account account = new Account();
        account.setCampusId(request.getCampusId());
        account.setLoginName(request.getLoginName());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setNickname(request.getNickname());
        account.setContactInfo(request.getContactInfo() != null ? request.getContactInfo() : "");
        account.setRole(0);
        account.setCertStatus(0);
        account.setReputation(0);
        account.setStatus(0);
        accountMapper.insert(account);

        PointAccount pointAccount = new PointAccount();
        pointAccount.setAccountId(account.getId());
        pointAccount.setBalance(0);
        pointAccountMapper.insert(pointAccount);

        return buildTokenResponse(account);
    }

    public AuthTokenResponse login(LoginRequest request) {
        Account account = accountMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getLoginName, request.getLoginName()));
        if (account == null || !passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new BusinessException(ErrorCode.USER_LOGIN_FAILED);
        }
        if (account.getStatus() != null && account.getStatus() == 1) {
            throw new BusinessException(ErrorCode.USER_LOCKED);
        }
        return buildTokenResponse(account);
    }

    private AuthTokenResponse buildTokenResponse(Account account) {
        String token = jwtTokenProvider.createAccessToken(account);
        return new AuthTokenResponse(account.getId(), token, jwtTokenProvider.getExpireSeconds());
    }

    private boolean existsByLoginName(String loginName) {
        return accountMapper.selectCount(new LambdaQueryWrapper<Account>()
                .eq(Account::getLoginName, loginName)) > 0;
    }

    private boolean existsByCampusId(String campusId) {
        return accountMapper.selectCount(new LambdaQueryWrapper<Account>()
                .eq(Account::getCampusId, campusId)) > 0;
    }
}
