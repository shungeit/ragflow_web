package com.ragflow.admin.controller;

import com.ragflow.admin.service.RagflowService;
import com.ragflow.admin.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/retrieval")
public class RetrievalController {

    private final RagflowService ragflowService;

    public RetrievalController(RagflowService ragflowService) {
        this.ragflowService = ragflowService;
    }

    @PostMapping
    public Result<Object> retrieveChunks(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = ragflowService.retrieveChunks(body, null);
        return Result.ok(resp.get("data"));
    }
}
