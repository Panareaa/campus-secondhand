package cn.ynu.campus.relife.trade.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.PageResult;
import cn.ynu.campus.relife.trade.domain.TradeNotify;
import cn.ynu.campus.relife.trade.dto.NotificationVO;
import cn.ynu.campus.relife.trade.mapper.TradeNotifyMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final TradeNotifyMapper tradeNotifyMapper;

    public NotificationService(TradeNotifyMapper tradeNotifyMapper) {
        this.tradeNotifyMapper = tradeNotifyMapper;
    }

    public void send(Long accountId, Long tradeId, String tradeNo, String notifyType, String title, String content) {
        TradeNotify notify = new TradeNotify();
        notify.setAccountId(accountId);
        notify.setTradeId(tradeId);
        notify.setTradeNo(tradeNo != null ? tradeNo : "");
        notify.setNotifyType(notifyType);
        notify.setTitle(title);
        notify.setContent(content != null ? content : "");
        notify.setIsRead(0);
        tradeNotifyMapper.insert(notify);
    }

    public PageResult<NotificationVO> list(Long accountId, int page, int size, Boolean isRead) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.PAGE_INVALID);
        }
        LambdaQueryWrapper<TradeNotify> wrapper = new LambdaQueryWrapper<TradeNotify>()
                .eq(TradeNotify::getAccountId, accountId)
                .orderByDesc(TradeNotify::getCreatedAt);
        if (isRead != null) {
            wrapper.eq(TradeNotify::getIsRead, Boolean.TRUE.equals(isRead) ? 1 : 0);
        }
        Page<TradeNotify> result = tradeNotifyMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords().stream().map(this::toVO).toList(),
                page, size, result.getTotal());
    }

    @Transactional
    public void markRead(Long accountId, Long notificationId) {
        TradeNotify notify = tradeNotifyMapper.selectById(notificationId);
        if (notify == null || !accountId.equals(notify.getAccountId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        notify.setIsRead(1);
        tradeNotifyMapper.updateById(notify);
    }

    @Transactional
    public void markAllRead(Long accountId) {
        tradeNotifyMapper.update(null, new LambdaUpdateWrapper<TradeNotify>()
                .eq(TradeNotify::getAccountId, accountId)
                .eq(TradeNotify::getIsRead, 0)
                .set(TradeNotify::getIsRead, 1));
    }

    private NotificationVO toVO(TradeNotify notify) {
        NotificationVO vo = new NotificationVO();
        vo.setNotificationId(notify.getId());
        vo.setNotifyType(notify.getNotifyType());
        vo.setTitle(notify.getTitle());
        vo.setContent(notify.getContent());
        vo.setTradeNo(notify.getTradeNo());
        vo.setIsRead(notify.getIsRead() != null && notify.getIsRead() == 1);
        vo.setCreatedAt(notify.getCreatedAt());
        return vo;
    }
}
