package com.example.aiservice.controller;

import com.example.aiservice.dto.ChatMessageRequest;
import com.example.aiservice.dto.ChatMessageResponse;
import com.example.aiservice.dto.SemanticSearchRequest;
import com.example.aiservice.dto.SemanticSearchResponse;
import com.example.aiservice.service.AiConciergeService;
import com.example.aiservice.service.RAGService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiConciergeService aiConciergeService;
    private final RAGService ragService;

    public AiController(AiConciergeService aiConciergeService, RAGService ragService) {
        this.aiConciergeService = aiConciergeService;
        this.ragService = ragService;
    }

    @PostMapping("/chat")
    public ChatMessageResponse chat(@Valid @RequestBody ChatMessageRequest request) {
        return aiConciergeService.chat(request);
    }

    @PostMapping("/search")
    public SemanticSearchResponse search(@Valid @RequestBody SemanticSearchRequest request) {
        int topK = request.topK() != null && request.topK() > 0 ? request.topK() : 3;
        List<Map<String, Object>> results = ragService.search(request.query(), topK);
        return new SemanticSearchResponse(request.query(), results.size(), results);
    }

    @PostMapping("/index")
    public Map<String, Object> indexHotels() {
        int count = ragService.indexAllHotels();
        return Map.of(
                "message", "Hotels indexed into VectorStore successfully",
                "indexedCount", count
        );
    }
}
