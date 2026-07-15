package cn.ynu.campus.relife.ai.service;

import cn.ynu.campus.relife.ai.client.ItemFeignClient;
import cn.ynu.campus.relife.ai.client.dto.FeignInternalItemSearchRequest;
import cn.ynu.campus.relife.ai.client.dto.FeignItemSummaryVO;
import cn.ynu.campus.relife.ai.dto.ParsedSearchCondition;
import cn.ynu.campus.relife.ai.dto.SearchRequest;
import cn.ynu.campus.relife.ai.dto.SearchResponse;
import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.common.core.result.PageResult;
import org.springframework.stereotype.Service;

@Service
public class AiSearchService {

    private final AiLlmService aiLlmService;
    private final AiDegradeService aiDegradeService;
    private final AiLogService aiLogService;
    private final ItemFeignClient itemFeignClient;

    public AiSearchService(AiLlmService aiLlmService,
                           AiDegradeService aiDegradeService,
                           AiLogService aiLogService,
                           ItemFeignClient itemFeignClient) {
        this.aiLlmService = aiLlmService;
        this.aiDegradeService = aiDegradeService;
        this.aiLogService = aiLogService;
        this.itemFeignClient = itemFeignClient;
    }

    public SearchResponse search(Long accountId, SearchRequest request) {
        long start = System.currentTimeMillis();
        var llmParsed = aiLlmService.parseSearchQuery(request.getQuery());
        ParsedSearchCondition condition;
        boolean degraded;
        if (llmParsed.isPresent()) {
            condition = llmParsed.get();
            degraded = false;
        } else {
            condition = aiDegradeService.parseSearchFallback(request.getQuery());
            degraded = true;
        }

        FeignInternalItemSearchRequest feignRequest = toFeignRequest(condition, request.getPage(), request.getSize());
        ApiResponse<PageResult<FeignItemSummaryVO>> response = itemFeignClient.search(feignRequest);
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
        }

        SearchResponse result = new SearchResponse();
        result.setParsedCondition(condition);
        result.setItems(response.getData());
        result.setDegraded(degraded);
        aiLogService.logSearch(accountId, request.getQuery(), condition,
                (int) response.getData().getTotal(), (int) (System.currentTimeMillis() - start));
        return result;
    }

    private FeignInternalItemSearchRequest toFeignRequest(ParsedSearchCondition condition, int page, int size) {
        FeignInternalItemSearchRequest req = new FeignInternalItemSearchRequest();
        req.setCategoryId(condition.getCategoryId());
        req.setKeywords(condition.getKeywords());
        req.setMinPrice(condition.getMinPrice());
        req.setMaxPrice(condition.getMaxPrice());
        req.setPage(page);
        req.setSize(size);
        req.setSort(condition.getSort() != null ? condition.getSort() : "latest");
        return req;
    }
}
