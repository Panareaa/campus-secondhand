package cn.ynu.campus.relife.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "campus.ai")
public class AiProperties {

    private boolean llmEnabled = false;
    private String modelName = "gpt-4o-mini";
    private long describeTimeoutMs = 8000;

    public boolean isLlmEnabled() {
        return llmEnabled;
    }

    public void setLlmEnabled(boolean llmEnabled) {
        this.llmEnabled = llmEnabled;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public long getDescribeTimeoutMs() {
        return describeTimeoutMs;
    }

    public void setDescribeTimeoutMs(long describeTimeoutMs) {
        this.describeTimeoutMs = describeTimeoutMs;
    }
}
