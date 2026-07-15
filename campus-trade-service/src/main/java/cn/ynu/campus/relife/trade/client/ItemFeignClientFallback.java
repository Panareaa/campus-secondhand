package cn.ynu.campus.relife.trade.client;

import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.trade.client.dto.FeignBatchSaleCheckRequest;
import cn.ynu.campus.relife.trade.client.dto.FeignItemSaleCheckVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItemFeignClientFallback implements ItemFeignClient {

    @Override
    public ApiResponse<FeignItemSaleCheckVO> saleCheck(Long itemId) {
        return ApiResponse.fail(ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode(), "物品服务繁忙");
    }

    @Override
    public ApiResponse<List<FeignItemSaleCheckVO>> batchSaleCheck(FeignBatchSaleCheckRequest request) {
        return ApiResponse.fail(ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode(), "物品服务繁忙");
    }
}
