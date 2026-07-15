package cn.ynu.campus.relife.ai.service;

import cn.ynu.campus.relife.ai.config.AiProperties;
import cn.ynu.campus.relife.ai.dto.DescribeRequest;
import cn.ynu.campus.relife.ai.dto.DescribeResponse;
import org.springframework.stereotype.Service;

@Service
public class AiDescribeService {

    private final AiLlmService aiLlmService;
    private final AiDegradeService aiDegradeService;
    private final AiLogService aiLogService;
    private final AiProperties aiProperties;

    public AiDescribeService(AiLlmService aiLlmService,
                             AiDegradeService aiDegradeService,
                             AiLogService aiLogService,
                             AiProperties aiProperties) {
        this.aiLlmService = aiLlmService;
        this.aiDegradeService = aiDegradeService;
        this.aiLogService = aiLogService;
        this.aiProperties = aiProperties;
    }

    public DescribeResponse describe(Long accountId, DescribeRequest request) {
        long start = System.currentTimeMillis();
        DescribeResponse response = new DescribeResponse();
        response.setSuggestedTags(aiDegradeService.suggestTags(request));

        var llmText = aiLlmService.generateDescribe(request);
        if (llmText.isPresent()) {
            response.setDescription(llmText.get());
            response.setDegraded(false);
            response.setModelName(aiProperties.getModelName());
            aiLogService.logDescribe(accountId, request, response, aiProperties.getModelName(),
                    (int) (System.currentTimeMillis() - start), AiLogService.STATUS_SUCCESS);
            return response;
        }

        response.setDescription(aiDegradeService.buildDescribe(request));
        response.setDegraded(true);
        response.setModelName("template");
        aiLogService.logDescribe(accountId, request, response, "template",
                (int) (System.currentTimeMillis() - start), AiLogService.STATUS_DEGRADED);
        return response;
    }
}
