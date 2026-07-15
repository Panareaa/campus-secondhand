package cn.ynu.campus.relife.trade.client;

import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.trade.client.dto.FeignLockStockRequest;
import cn.ynu.campus.relife.trade.client.dto.FeignReleaseStockRequest;
import cn.ynu.campus.relife.trade.client.dto.FeignStockAvailableVO;
import cn.ynu.campus.relife.trade.client.dto.FeignStockLockResultVO;
import org.springframework.stereotype.Component;

@Component
public class StockFeignClientFallback implements StockFeignClient {

    @Override
    public ApiResponse<FeignStockAvailableVO> available(Long itemId) {
        return ApiResponse.fail(ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode(), "库存服务繁忙");
    }

    @Override
    public ApiResponse<FeignStockLockResultVO> lock(FeignLockStockRequest request) {
        return ApiResponse.fail(ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode(), "库存服务繁忙");
    }

    @Override
    public ApiResponse<Void> release(FeignReleaseStockRequest request) {
        return ApiResponse.fail(ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode(), "库存服务繁忙");
    }
}
