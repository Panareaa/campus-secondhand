package cn.ynu.campus.relife.trade.controller;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.trade.support.TradeShardHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/trade")
public class TradeShardInternalController {

    @GetMapping("/shard-route")
    public ApiResponse<Map<String, Object>> shardRoute(@RequestParam Long buyerId) {
        return ApiResponse.ok(Map.of(
                "buyerId", buyerId,
                "orderTable", TradeShardHelper.orderTable(buyerId),
                "lineTable", TradeShardHelper.lineTable(buyerId),
                "algorithm", "MOD(buyer_id, 2)",
                "shardingSphere", true
        ));
    }
}
