package com.example.aiservice.dto;

import java.util.List;
import java.util.Map;

public record SemanticSearchResponse(
        String query,
        int count,
        List<Map<String, Object>> results
) {}
