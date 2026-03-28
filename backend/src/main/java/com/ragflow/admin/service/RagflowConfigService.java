package com.ragflow.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ragflow.admin.entity.SysConfig;
import com.ragflow.admin.mapper.SysConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RagflowConfigService {

    private static final Logger log = LoggerFactory.getLogger(RagflowConfigService.class);

    private static final String KEY_BASE_URL = "ragflow.base-url";
    private static final String KEY_API_KEY = "ragflow.api-key";

    private final SysConfigMapper configMapper;

    @Value("${ragflow.base-url:}")
    private String fallbackBaseUrl;

    @Value("${ragflow.api-key:}")
    private String fallbackApiKey;

    // Cache WebClient per baseUrl to avoid creating new ones every request
    private final ConcurrentHashMap<String, WebClient> clientCache = new ConcurrentHashMap<>();

    public RagflowConfigService(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    public String getBaseUrl() {
        String val = getConfigValue(KEY_BASE_URL);
        return (val != null && !val.isBlank()) ? val : fallbackBaseUrl;
    }

    public String getApiKey() {
        String val = getConfigValue(KEY_API_KEY);
        return (val != null && !val.isBlank()) ? val : fallbackApiKey;
    }

    public void setBaseUrl(String url) {
        saveConfig(KEY_BASE_URL, url, "RAGFlow 服务地址");
        // Clear cached WebClient since URL changed
        clientCache.clear();
    }

    public void setApiKey(String key) {
        saveConfig(KEY_API_KEY, key, "RAGFlow API 密钥");
    }

    /**
     * Get a WebClient for the current RAGFlow base URL.
     * Cached per URL to avoid creating new clients every request.
     */
    public WebClient getWebClient() {
        String baseUrl = getBaseUrl();
        return clientCache.computeIfAbsent(baseUrl, url -> {
            log.info("Creating WebClient for RAGFlow: {}", url);
            HttpClient httpClient = HttpClient.create()
                    .responseTimeout(Duration.ofMinutes(5));
            return WebClient.builder()
                    .baseUrl(url)
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                    .build();
        });
    }

    /**
     * Resolve API key: prefer user-specific key, fallback to system config
     */
    public String resolveApiKey(String userApiKey) {
        if (userApiKey != null && !userApiKey.isBlank()) return userApiKey;
        return getApiKey();
    }

    private String getConfigValue(String key) {
        SysConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : null;
    }

    private void saveConfig(String key, String value, String remark) {
        SysConfig existing = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (existing != null) {
            existing.setConfigValue(value);
            configMapper.updateById(existing);
        } else {
            SysConfig config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setRemark(remark);
            configMapper.insert(config);
        }
    }
}
