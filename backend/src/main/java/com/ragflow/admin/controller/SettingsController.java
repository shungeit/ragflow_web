package com.ragflow.admin.controller;

import com.ragflow.admin.service.RagflowConfigService;
import com.ragflow.admin.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final RagflowConfigService configService;

    public SettingsController(RagflowConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/ragflow")
    public Result<Map<String, Object>> getRagflowConfig() {
        Map<String, Object> data = new HashMap<>();
        data.put("baseUrl", configService.getBaseUrl());
        data.put("apiKey", configService.getApiKey());
        return Result.ok(data);
    }

    @PutMapping("/ragflow")
    public Result<Object> updateRagflowConfig(@RequestBody Map<String, String> body) {
        String baseUrl = body.get("baseUrl");
        String apiKey = body.get("apiKey");
        if (baseUrl != null && !baseUrl.isBlank()) {
            // Remove trailing slash
            configService.setBaseUrl(baseUrl.replaceAll("/+$", ""));
        }
        if (apiKey != null && !apiKey.isBlank()) {
            configService.setApiKey(apiKey);
        }
        return Result.ok();
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> checkHealth() {
        Map<String, Object> data = new HashMap<>();
        try {
            Map resp = configService.getWebClient().get()
                    .uri("/api/v1/datasets?page=1&page_size=1")
                    .header("Authorization", "Bearer " + configService.getApiKey())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            boolean ok = resp != null && (Integer.valueOf(0).equals(resp.get("code")));
            data.put("ok", ok);
            data.put("baseUrl", configService.getBaseUrl());
        } catch (Exception e) {
            data.put("ok", false);
            data.put("error", e.getMessage());
            data.put("baseUrl", configService.getBaseUrl());
        }
        return Result.ok(data);
    }
}
