package com.ragflow.admin.controller;

import com.ragflow.admin.service.RagflowService;
import com.ragflow.admin.util.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final RagflowService ragflowService;

    public DatasetController(RagflowService ragflowService) {
        this.ragflowService = ragflowService;
    }

    @GetMapping
    public Result<Object> listDatasets() {
        Map<String, Object> resp = ragflowService.listDatasets(null);
        return Result.ok(resp.get("data"));
    }

    @PostMapping
    public Result<Object> createDataset(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = ragflowService.createDataset(body, null);
        return Result.ok(resp.get("data"));
    }

    @DeleteMapping
    public Result<Object> deleteDatasets(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.err("ids is required");
        }
        Map<String, Object> resp = ragflowService.deleteDatasets(ids, null);
        return Result.ok(resp);
    }

    @GetMapping("/{id}/documents")
    public Result<Object> listDocuments(@PathVariable String id) {
        Map<String, Object> resp = ragflowService.listDocuments(id, null);
        return Result.ok(resp.get("data"));
    }

    @PutMapping("/{datasetId}")
    public Result<Object> updateDataset(@PathVariable String datasetId, @RequestBody Map<String, Object> body) {
        Map<String, Object> resp = ragflowService.updateDataset(datasetId, body, null);
        return Result.ok(resp.get("data"));
    }

    @PostMapping("/{datasetId}/documents/upload")
    public Result<Object> uploadDocuments(@PathVariable String datasetId, @RequestParam("file") MultipartFile[] files) {
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                Map<String, Object> resp = ragflowService.uploadDocuments(datasetId, file.getBytes(), file.getOriginalFilename(), null);
                results.add(resp);
            }
        } catch (Exception e) {
            return Result.err("Upload failed: " + e.getMessage());
        }
        return Result.ok(results);
    }

    @DeleteMapping("/{datasetId}/documents")
    public Result<Object> deleteDocuments(@PathVariable String datasetId, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.err("ids is required");
        }
        Map<String, Object> resp = ragflowService.deleteDocuments(datasetId, ids, null);
        return Result.ok(resp);
    }

    @PostMapping("/{datasetId}/documents/parse")
    public Result<Object> parseDocuments(@PathVariable String datasetId, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> documentIds = (List<String>) body.get("document_ids");
        if (documentIds == null || documentIds.isEmpty()) {
            return Result.err("document_ids is required");
        }
        Map<String, Object> resp = ragflowService.parseDocuments(datasetId, documentIds, null);
        return Result.ok(resp);
    }

    @DeleteMapping("/{datasetId}/documents/parse")
    public Result<Object> stopParsingDocuments(@PathVariable String datasetId, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> documentIds = (List<String>) body.get("document_ids");
        if (documentIds == null || documentIds.isEmpty()) {
            return Result.err("document_ids is required");
        }
        Map<String, Object> resp = ragflowService.stopParsingDocuments(datasetId, documentIds, null);
        return Result.ok(resp);
    }

    @PutMapping("/{datasetId}/documents/{documentId}")
    public Result<Object> updateDocument(@PathVariable String datasetId, @PathVariable String documentId, @RequestBody Map<String, Object> body) {
        Map<String, Object> resp = ragflowService.updateDocument(datasetId, documentId, body, null);
        return Result.ok(resp.get("data"));
    }

    @GetMapping("/{datasetId}/documents/{documentId}/chunks")
    public Result<Object> listChunks(@PathVariable String datasetId, @PathVariable String documentId) {
        Map<String, Object> resp = ragflowService.listChunks(datasetId, documentId, null);
        return Result.ok(resp.get("data"));
    }
}
