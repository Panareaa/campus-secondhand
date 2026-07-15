package cn.ynu.campus.relife.ai.dto;

import cn.ynu.campus.relife.common.core.result.PageResult;
import cn.ynu.campus.relife.ai.client.dto.FeignItemSummaryVO;

public class SearchResponse {

    private ParsedSearchCondition parsedCondition;
    private PageResult<FeignItemSummaryVO> items;
    private boolean degraded;

    public ParsedSearchCondition getParsedCondition() {
        return parsedCondition;
    }

    public void setParsedCondition(ParsedSearchCondition parsedCondition) {
        this.parsedCondition = parsedCondition;
    }

    public PageResult<FeignItemSummaryVO> getItems() {
        return items;
    }

    public void setItems(PageResult<FeignItemSummaryVO> items) {
        this.items = items;
    }

    public boolean isDegraded() {
        return degraded;
    }

    public void setDegraded(boolean degraded) {
        this.degraded = degraded;
    }
}
