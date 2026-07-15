package cn.ynu.campus.relife.user.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.PageResult;
import cn.ynu.campus.relife.user.domain.PointAccount;
import cn.ynu.campus.relife.user.domain.PointLedger;
import cn.ynu.campus.relife.user.dto.PointBalanceVO;
import cn.ynu.campus.relife.user.dto.PointGrantRequest;
import cn.ynu.campus.relife.user.dto.PointGrantResultVO;
import cn.ynu.campus.relife.user.dto.PointLedgerVO;
import cn.ynu.campus.relife.user.mapper.PointAccountMapper;
import cn.ynu.campus.relife.user.mapper.PointLedgerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointService {

    private final PointAccountMapper pointAccountMapper;
    private final PointLedgerMapper pointLedgerMapper;

    public PointService(PointAccountMapper pointAccountMapper, PointLedgerMapper pointLedgerMapper) {
        this.pointAccountMapper = pointAccountMapper;
        this.pointLedgerMapper = pointLedgerMapper;
    }

    public PointBalanceVO getBalance(Long accountId) {
        PointAccount account = requireAccount(accountId);
        return new PointBalanceVO(account.getBalance(), account.getUpdatedAt());
    }

    public PageResult<PointLedgerVO> listLedger(Long accountId, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.PAGE_INVALID);
        }
        Page<PointLedger> result = pointLedgerMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<PointLedger>()
                        .eq(PointLedger::getAccountId, accountId)
                        .orderByDesc(PointLedger::getCreatedAt));
        return new PageResult<>(result.getRecords().stream().map(this::toVO).toList(),
                page, size, result.getTotal());
    }

    @Transactional
    public PointGrantResultVO grant(PointGrantRequest request) {
        PointLedger existing = pointLedgerMapper.selectOne(new LambdaQueryWrapper<PointLedger>()
                .eq(PointLedger::getIdempotentKey, request.getIdempotentKey()));
        if (existing != null) {
            PointAccount account = requireAccount(existing.getAccountId());
            return new PointGrantResultVO(existing.getId(), account.getBalance(), true);
        }

        PointAccount account = requireAccount(request.getAccountId());
        int newBalance = account.getBalance() + request.getChangeAmount();
        account.setBalance(newBalance);
        pointAccountMapper.updateById(account);

        PointLedger ledger = new PointLedger();
        ledger.setAccountId(request.getAccountId());
        ledger.setTradeId(request.getTradeId());
        ledger.setChangeAmount(request.getChangeAmount());
        ledger.setBalanceAfter(newBalance);
        ledger.setRuleCode(request.getRuleCode());
        ledger.setIdempotentKey(request.getIdempotentKey());
        ledger.setRemark(request.getRemark() != null ? request.getRemark() : "");
        try {
            pointLedgerMapper.insert(ledger);
        } catch (DuplicateKeyException ex) {
            PointLedger dup = pointLedgerMapper.selectOne(new LambdaQueryWrapper<PointLedger>()
                    .eq(PointLedger::getIdempotentKey, request.getIdempotentKey()));
            PointAccount refreshed = requireAccount(request.getAccountId());
            return new PointGrantResultVO(dup.getId(), refreshed.getBalance(), true);
        }
        return new PointGrantResultVO(ledger.getId(), newBalance, false);
    }

    private PointAccount requireAccount(Long accountId) {
        PointAccount account = pointAccountMapper.selectOne(new LambdaQueryWrapper<PointAccount>()
                .eq(PointAccount::getAccountId, accountId));
        if (account == null) {
            PointAccount created = new PointAccount();
            created.setAccountId(accountId);
            created.setBalance(0);
            pointAccountMapper.insert(created);
            return created;
        }
        return account;
    }

    private PointLedgerVO toVO(PointLedger ledger) {
        PointLedgerVO vo = new PointLedgerVO();
        vo.setLedgerId(ledger.getId());
        vo.setChangeAmount(ledger.getChangeAmount());
        vo.setBalanceAfter(ledger.getBalanceAfter());
        vo.setRuleCode(ledger.getRuleCode());
        vo.setRemark(ledger.getRemark());
        vo.setTradeId(ledger.getTradeId());
        vo.setCreatedAt(ledger.getCreatedAt());
        return vo;
    }
}
