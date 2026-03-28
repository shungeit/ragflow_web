package com.ragflow.admin.controller;

import com.ragflow.admin.service.RagflowService;
import com.ragflow.admin.util.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RagflowService ragflowService;

    public ChatController(RagflowService ragflowService) {
        this.ragflowService = ragflowService;
    }

    @GetMapping("/assistants")
    public Result<Object> listAssistants() {
        Map<String, Object> resp = ragflowService.listChatAssistants(null);
        return Result.ok(resp.get("data"));
    }

    @PostMapping("/assistants")
    public Result<Object> createAssistant(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = ragflowService.createChatAssistant(body, null);
        return Result.ok(resp.get("data"));
    }

    @PutMapping("/assistants/{chatId}")
    public Result<Object> updateAssistant(@PathVariable String chatId, @RequestBody Map<String, Object> body) {
        Map<String, Object> resp = ragflowService.updateChatAssistant(chatId, body, null);
        return Result.ok(resp.get("data"));
    }

    @DeleteMapping("/assistants")
    public Result<Object> deleteAssistants(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.err("ids is required");
        }
        Map<String, Object> resp = ragflowService.deleteChatAssistants(ids, null);
        return Result.ok(resp);
    }

    @GetMapping("/agents")
    public Result<Object> listAgents() {
        Map<String, Object> resp = ragflowService.listAgents(null);
        return Result.ok(resp.get("data"));
    }

    @PostMapping("/sessions")
    public Result<Object> createSession(@RequestBody Map<String, String> body) {
        String chatId = body.get("chatId");
        String agentId = body.get("agentId");
        String type = body.getOrDefault("type", "chat");

        Map<String, Object> resp;
        if ("agent".equals(type) && agentId != null && !agentId.isBlank()) {
            resp = ragflowService.createAgentSession(agentId, null);
        } else if (chatId != null && !chatId.isBlank()) {
            resp = ragflowService.createSession(chatId, null);
        } else {
            return Result.err("chatId or agentId is required");
        }
        return Result.ok(resp.get("data"));
    }

    @GetMapping("/sessions")
    public Result<Object> listSessions(
            @RequestParam(required = false) String chatId,
            @RequestParam(required = false) String agentId) {
        Map<String, Object> resp;
        if (agentId != null && !agentId.isBlank()) {
            resp = ragflowService.listAgentSessions(agentId, null);
        } else if (chatId != null && !chatId.isBlank()) {
            resp = ragflowService.listSessions(chatId, null);
        } else {
            return Result.err("chatId or agentId is required");
        }
        return Result.ok(resp.get("data"));
    }

    @PutMapping("/sessions")
    public Result<Object> updateSession(@RequestBody Map<String, String> body) {
        String chatId = body.get("chatId");
        String sessionId = body.get("sessionId");
        String name = body.get("name");
        if (chatId == null || chatId.isBlank() || sessionId == null || sessionId.isBlank()) {
            return Result.err("chatId and sessionId are required");
        }
        Map<String, Object> resp = ragflowService.updateSession(chatId, sessionId, name, null);
        return Result.ok(resp.get("data"));
    }

    @DeleteMapping("/sessions")
    public Result<Object> deleteSessions(@RequestBody Map<String, Object> body) {
        String chatId = (String) body.get("chatId");
        @SuppressWarnings("unchecked")
        List<String> sessionIds = (List<String>) body.get("sessionIds");
        if (chatId == null || chatId.isBlank() || sessionIds == null || sessionIds.isEmpty()) {
            return Result.err("chatId and sessionIds are required");
        }
        Map<String, Object> resp = ragflowService.deleteSession(chatId, sessionIds, null);
        return Result.ok(resp);
    }

    @PostMapping("/completions")
    public Object chatCompletions(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        String chatId = (String) body.get("chatId");
        String sessionId = (String) body.get("sessionId");
        String question = (String) body.get("question");
        Boolean stream = (Boolean) body.getOrDefault("stream", false);

        if (chatId == null || chatId.isBlank() || question == null || question.isBlank()) {
            return Result.err("chatId and question are required");
        }

        if (Boolean.TRUE.equals(stream)) {
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("X-Accel-Buffering", "no");
            return ragflowService.chatStream(chatId, sessionId, question, null);
        }

        Map<String, Object> resp = ragflowService.chat(chatId, sessionId, question, null);
        return Result.ok(resp.get("data"));
    }

    @PostMapping(value = "/completions/agent")
    public Object chatAgentCompletions(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        String agentId = (String) body.get("agentId");
        String sessionId = (String) body.get("sessionId");
        String question = (String) body.get("question");
        Boolean stream = (Boolean) body.getOrDefault("stream", false);

        if (agentId == null || agentId.isBlank() || question == null || question.isBlank()) {
            return Result.err("agentId and question are required");
        }

        if (Boolean.TRUE.equals(stream)) {
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("X-Accel-Buffering", "no");
            return ragflowService.chatAgentStream(agentId, sessionId, question, null);
        }

        Map<String, Object> resp = ragflowService.chatAgent(agentId, sessionId, question, null);
        return Result.ok(resp.get("data"));
    }
}
