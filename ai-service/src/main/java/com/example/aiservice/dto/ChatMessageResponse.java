package com.example.aiservice.dto;

import java.util.List;

public record ChatMessageResponse(
        String reply,
        String conversationId,
        List<String> toolsUsed
) {}
