package com.example.aiservice.dto;

import jakarta.validation.constraints.NotBlank;

public record SemanticSearchRequest(
        @NotBlank String query,
        Integer topK
) {}
