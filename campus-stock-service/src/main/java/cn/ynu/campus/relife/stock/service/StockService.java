package cn.ynu.campus.relife.stock.service;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.stock.domain.StockLockLog;
import cn.ynu.campus.relife.stock.domain.StockRecord;
import cn.ynu.campus.relife.stock.dto.InitStockRequest;
import cn.ynu.campus.relife.stock.dto.LockStockRequest;
import cn.ynu.campus.relife.stock.dto.ReleaseStockRequest;
import cn.ynu.campus.relife.stock.dto.StockAvailableVO;
import cn.ynu.campus.relife.stock.dto.StockLockResultVO;
import cn.ynu.campus.relife.stock.mapper.StockLockLogMapper;
import cn.ynu.campus.relife.stock.mapper.StockRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class StockService {

    private static final int LOCK_ACTIVE = 1;
    private static final int LOCK_RELEASED = 2;

    private final StockRecordMapper stockRecordMapper;
    private final StockLockLogMapper stockLockLogMapper;

    public StockService(StockRecordMapper stockRecordMapper, StockLockLogMapper stockLockLogMapper) {
        this.stockRecordMapper = stockRecordMapper;
        this.stockLockLogMapper = stockLockLogMapper;
    }

    @Transactional
    public StockAvailableVO initStock(InitStockRequest request) {
        StockRecord existing = findByItemId(request.getItemId());
        if (existing != null) {
            return toVO(existing);
        }
        StockRecord record = new StockRecord();
        record.setItemId(request.getItemId());
        record.setTotalQty(request.getTotalQty());
        record.setLockedQty(0);
        record.setAvailableQty(request.getTotalQty());
        record.setVersion(0);
        stockRecordMapper.insert(record);
        return toVO(record);
    }

    public StockAvailableVO getAvailable(Long itemId) {
        StockRecord record = findByItemId(itemId);
        if (record == null) {
            throw new BusinessException(ErrorCode.STOCK_RECORD_NOT_FOUND);
        }
        return toVO(record);
    }

    @Transactional
    public StockLockResultVO lockStock(LockStockRequest request) {
        StockLockLog existing = stockLockLogMapper.selectOne(new LambdaQueryWrapper<StockLockLog>()
                .eq(StockLockLog::getTradeId, request.getTradeId())
                .eq(StockLockLog::getItemId, request.getItemId()));
        if (existing != null && existing.getLockStatus() == LOCK_ACTIVE) {
            return new StockLockResultVO(existing.getId(), true, true);
        }

        StockRecord record = findByItemId(request.getItemId());
        if (record == null) {
            throw new BusinessException(ErrorCode.STOCK_RECORD_NOT_FOUND);
        }
        if (record.getAvailableQty() == null || record.getAvailableQty() < request.getQuantity()) {
            throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
        }

        int updated = stockRecordMapper.update(null, new LambdaUpdateWrapper<StockRecord>()
                .setSql("locked_qty = locked_qty + " + request.getQuantity())
                .setSql("available_qty = available_qty - " + request.getQuantity())
                .eq(StockRecord::getItemId, request.getItemId())
                .ge(StockRecord::getAvailableQty, request.getQuantity())
                .eq(StockRecord::getVersion, record.getVersion())
                .set(StockRecord::getVersion, record.getVersion() + 1));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.STOCK_LOCK_FAILED);
        }

        StockLockLog log = new StockLockLog();
        log.setItemId(request.getItemId());
        log.setTradeId(request.getTradeId());
        log.setTradeNo(request.getTradeNo());
        log.setLockQty(request.getQuantity());
        log.setLockStatus(LOCK_ACTIVE);
        stockLockLogMapper.insert(log);
        return new StockLockResultVO(log.getId(), true, false);
    }

    @Transactional
    public void releaseStock(ReleaseStockRequest request) {
        StockLockLog log = stockLockLogMapper.selectOne(new LambdaQueryWrapper<StockLockLog>()
                .eq(StockLockLog::getTradeId, request.getTradeId())
                .eq(StockLockLog::getItemId, request.getItemId()));
        if (log == null || log.getLockStatus() == LOCK_RELEASED) {
            return;
        }

        StockRecord record = findByItemId(request.getItemId());
        if (record != null) {
            stockRecordMapper.update(null, new LambdaUpdateWrapper<StockRecord>()
                    .setSql("locked_qty = locked_qty - " + log.getLockQty())
                    .setSql("available_qty = available_qty + " + log.getLockQty())
                    .eq(StockRecord::getItemId, request.getItemId())
                    .ge(StockRecord::getLockedQty, log.getLockQty()));
        }

        log.setLockStatus(LOCK_RELEASED);
        log.setReleasedAt(LocalDateTime.now());
        stockLockLogMapper.updateById(log);
    }

    private StockRecord findByItemId(Long itemId) {
        return stockRecordMapper.selectOne(new LambdaQueryWrapper<StockRecord>()
                .eq(StockRecord::getItemId, itemId));
    }

    private StockAvailableVO toVO(StockRecord record) {
        return new StockAvailableVO(record.getItemId(), record.getTotalQty(),
                record.getLockedQty(), record.getAvailableQty());
    }
}
