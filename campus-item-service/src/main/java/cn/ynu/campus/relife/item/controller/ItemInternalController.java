package cn.ynu.campus.relife.item.controller;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.item.dto.BatchSaleCheckRequest;
import cn.ynu.campus.relife.item.dto.InternalItemSearchRequest;
import cn.ynu.campus.relife.item.dto.ItemSaleCheckVO;
import cn.ynu.campus.relife.item.service.DataSourceProbeService;
import cn.ynu.campus.relife.item.service.ItemQueryService;
import cn.ynu.campus.relife.item.service.ItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/items")
public class ItemInternalController {

    private final ItemService itemService;
    private final ItemQueryService itemQueryService;
    private final DataSourceProbeService dataSourceProbeService;

    public ItemInternalController(ItemService itemService,
                                  ItemQueryService itemQueryService,
                                  DataSourceProbeService dataSourceProbeService) {
        this.itemService = itemService;
        this.itemQueryService = itemQueryService;
        this.dataSourceProbeService = dataSourceProbeService;
    }

    @GetMapping("/datasource-route")
    public ApiResponse<Map<String, Object>> datasourceRoute() {
        return ApiResponse.ok(dataSourceProbeService.probe());
    }

    @GetMapping("/{itemId}/sale-check")
    public ApiResponse<ItemSaleCheckVO> saleCheck(@PathVariable Long itemId) {
        return ApiResponse.ok(itemService.saleCheck(itemId));
    }

    @PostMapping("/sale-check/batch")
    public ApiResponse<List<ItemSaleCheckVO>> batchSaleCheck(@RequestBody BatchSaleCheckRequest request) {
        return ApiResponse.ok(itemService.batchSaleCheck(request.getItemIds()));
    }

    @PostMapping("/search")
    public ApiResponse<cn.ynu.campus.relife.common.core.result.PageResult<cn.ynu.campus.relife.item.dto.ItemSummaryVO>> search(
            @RequestBody InternalItemSearchRequest request) {
        return ApiResponse.ok(itemQueryService.searchInternal(request));
    }
}
