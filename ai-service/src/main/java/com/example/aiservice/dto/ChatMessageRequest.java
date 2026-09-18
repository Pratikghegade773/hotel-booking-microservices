package com.example.aiservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank String message,
        String conversationId
) {}
