package com.ragflow.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagflowService {

    private static final Logger log = LoggerFactory.getLogger(RagflowService.class);

    private final RagflowConfigService configService;

    public RagflowService(RagflowConfigService configService) {
        this.configService = configService;
    }

    private WebClient client() {
        return configService.getWebClient();
    }

    private String apiKey(String userApiKey) {
        return configService.resolveApiKey(userApiKey);
    }

    // ==================== Chat Assistants ====================

    public Map<String, Object> listChatAssistants(String userApiKey) {
        return client().get()
                .uri("/api/v1/chats")
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ==================== Agents ====================

    public Map<String, Object> listAgents(String userApiKey) {
        return client().get()
                .uri("/api/v1/agents")
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ==================== Sessions ====================

    public Map<String, Object> createSession(String chatId, String userApiKey) {
        return client().post()
                .uri("/api/v1/chats/{chatId}/sessions", chatId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> createAgentSession(String agentId, String userApiKey) {
        return client().post()
                .uri("/api/v1/agents/{agentId}/sessions", agentId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> listSessions(String chatId, String userApiKey) {
        return client().get()
                .uri("/api/v1/chats/{chatId}/sessions", chatId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> listAgentSessions(String agentId, String userApiKey) {
        return client().get()
                .uri("/api/v1/agents/{agentId}/sessions", agentId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> deleteSession(String chatId, List<String> sessionIds, String userApiKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("ids", sessionIds);
        return client().method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/v1/chats/{chatId}/sessions", chatId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> updateSession(String chatId, String sessionId, String name, String userApiKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        return client().put()
                .uri("/api/v1/chats/{chatId}/sessions/{sessionId}", chatId, sessionId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ==================== Chat Completions ====================

    public Map<String, Object> chat(String chatId, String sessionId, String question, String userApiKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("question", question);
        body.put("session_id", sessionId);
        body.put("stream", false);
        return client().post()
                .uri("/api/v1/chats/{chatId}/completions", chatId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public ResponseBodyEmitter chatStream(String chatId, String sessionId, String question, String userApiKey) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(300000L);

        Map<String, Object> body = new HashMap<>();
        body.put("question", question);
        body.put("session_id", sessionId);
        body.put("stream", true);

        Flux<DataBuffer> flux = client().post()
                .uri("/api/v1/chats/{chatId}/completions", chatId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(DataBuffer.class);

        flux.subscribe(
                dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        emitter.send(new String(bytes, StandardCharsets.UTF_8), MediaType.TEXT_EVENT_STREAM);
                    } catch (Exception e) {
                        log.warn("SSE send error: {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("RAGFlow stream error: {}", error.getMessage());
                    emitter.completeWithError(error);
                },
                emitter::complete
        );

        emitter.onTimeout(emitter::complete);
        return emitter;
    }

    public Map<String, Object> chatAgent(String agentId, String sessionId, String question, String userApiKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("question", question);
        body.put("session_id", sessionId);
        body.put("stream", false);
        return client().post()
                .uri("/api/v1/agents/{agentId}/completions", agentId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public ResponseBodyEmitter chatAgentStream(String agentId, String sessionId, String question, String userApiKey) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(300000L);

        Map<String, Object> body = new HashMap<>();
        body.put("question", question);
        body.put("session_id", sessionId);
        body.put("stream", true);

        Flux<DataBuffer> flux = client().post()
                .uri("/api/v1/agents/{agentId}/completions", agentId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(DataBuffer.class);

        flux.subscribe(
                dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        emitter.send(new String(bytes, StandardCharsets.UTF_8), MediaType.TEXT_EVENT_STREAM);
                    } catch (Exception e) {
                        log.warn("SSE send error: {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("RAGFlow agent stream error: {}", error.getMessage());
                    emitter.completeWithError(error);
                },
                emitter::complete
        );

        emitter.onTimeout(emitter::complete);
        return emitter;
    }

    // ==================== Datasets ====================

    public Map<String, Object> listDatasets(String userApiKey) {
        return client().get()
                .uri("/api/v1/datasets")
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> createDataset(Map<String, Object> params, String userApiKey) {
        return client().post()
                .uri("/api/v1/datasets")
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> deleteDatasets(List<String> ids, String userApiKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("ids", ids);
        return client().method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/v1/datasets")
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> listDocuments(String datasetId, String userApiKey) {
        return client().get()
                .uri("/api/v1/datasets/{datasetId}/documents", datasetId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> updateDataset(String datasetId, Map<String, Object> params, String userApiKey) {
        return client().put()
                .uri("/api/v1/datasets/{datasetId}", datasetId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ==================== Document Management ====================

    public Map<String, Object> uploadDocuments(String datasetId, byte[] fileBytes, String filename, String userApiKey) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        }).header("Content-Disposition", "form-data; name=file; filename=" + filename);

        return client().post()
                .uri("/api/v1/datasets/{datasetId}/documents", datasetId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> deleteDocuments(String datasetId, List<String> ids, String userApiKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("ids", ids);
        return client().method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/v1/datasets/{datasetId}/documents", datasetId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> parseDocuments(String datasetId, List<String> documentIds, String userApiKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("document_ids", documentIds);
        return client().post()
                .uri("/api/v1/datasets/{datasetId}/chunks", datasetId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> stopParsingDocuments(String datasetId, List<String> documentIds, String userApiKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("document_ids", documentIds);
        return client().method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/v1/datasets/{datasetId}/chunks", datasetId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> updateDocument(String datasetId, String documentId, Map<String, Object> params, String userApiKey) {
        return client().put()
                .uri("/api/v1/datasets/{datasetId}/documents/{documentId}", datasetId, documentId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ==================== Chunk Management ====================

    public Map<String, Object> listChunks(String datasetId, String documentId, String userApiKey) {
        return client().get()
                .uri("/api/v1/datasets/{datasetId}/documents/{documentId}/chunks", datasetId, documentId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> addChunk(String datasetId, String documentId, Map<String, Object> params, String userApiKey) {
        return client().post()
                .uri("/api/v1/datasets/{datasetId}/documents/{documentId}/chunks", datasetId, documentId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> deleteChunks(String datasetId, String documentId, List<String> chunkIds, String userApiKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("chunk_ids", chunkIds);
        return client().method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/v1/datasets/{datasetId}/documents/{documentId}/chunks", datasetId, documentId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ==================== Chat Assistant Management ====================

    public Map<String, Object> createChatAssistant(Map<String, Object> params, String userApiKey) {
        return client().post()
                .uri("/api/v1/chats")
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> updateChatAssistant(String chatId, Map<String, Object> params, String userApiKey) {
        return client().put()
                .uri("/api/v1/chats/{chatId}", chatId)
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> deleteChatAssistants(List<String> ids, String userApiKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("ids", ids);
        return client().method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/v1/chats")
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ==================== Retrieval ====================

    public Map<String, Object> retrieveChunks(Map<String, Object> params, String userApiKey) {
        return client().post()
                .uri("/api/v1/retrieval")
                .header("Authorization", "Bearer " + apiKey(userApiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
