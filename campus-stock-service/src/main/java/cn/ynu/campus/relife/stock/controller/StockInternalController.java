package cn.ynu.campus.relife.stock.controller;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.stock.dto.InitStockRequest;
import cn.ynu.campus.relife.stock.dto.LockStockRequest;
import cn.ynu.campus.relife.stock.dto.ReleaseStockRequest;
import cn.ynu.campus.relife.stock.dto.StockAvailableVO;
import cn.ynu.campus.relife.stock.dto.StockLockResultVO;
import cn.ynu.campus.relife.stock.service.StockService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/stock")
public class StockInternalController {

    private final StockService stockService;

    public StockInternalController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/init")
    public ApiResponse<StockAvailableVO> init(@Valid @RequestBody InitStockRequest request) {
        return ApiResponse.ok(stockService.initStock(request));
    }

    @GetMapping("/{itemId}/available")
    public ApiResponse<StockAvailableVO> available(@PathVariable Long itemId) {
        return ApiResponse.ok(stockService.getAvailable(itemId));
    }

    @PostMapping("/lock")
    public ApiResponse<StockLockResultVO> lock(@Valid @RequestBody LockStockRequest request) {
        return ApiResponse.ok(stockService.lockStock(request));
    }

    @PostMapping("/release")
    public ApiResponse<Void> release(@Valid @RequestBody ReleaseStockRequest request) {
        stockService.releaseStock(request);
        return ApiResponse.ok(null);
    }
}
