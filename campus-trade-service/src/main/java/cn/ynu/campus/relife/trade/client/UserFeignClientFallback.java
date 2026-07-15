package cn.ynu.campus.relife.trade.client;

import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.trade.client.dto.FeignPointGrantRequest;
import cn.ynu.campus.relife.trade.client.dto.FeignPointGrantResultVO;
import cn.ynu.campus.relife.trade.client.dto.FeignUserValidateVO;
import org.springframework.stereotype.Component;

@Component
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public ApiResponse<FeignUserValidateVO> validate(Long userId) {
        return ApiResponse.fail(ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode(), "用户服务繁忙");
    }

    @Override
    public ApiResponse<FeignPointGrantResultVO> grantPoints(FeignPointGrantRequest request) {
        return ApiResponse.fail(ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode(), "用户服务繁忙");
    }
}
