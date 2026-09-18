package com.example.aiservice.service;

import com.example.aiservice.dto.ChatMessageRequest;
import com.example.aiservice.dto.ChatMessageResponse;
import com.example.aiservice.tools.HotelBookingTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AiConciergeService {

    private static final Logger log = LoggerFactory.getLogger(AiConciergeService.class);

    private final ChatClient chatClient;
    private final HotelBookingTools hotelBookingTools;

    public AiConciergeService(
            @Autowired(required = false) ChatClient chatClient,
            HotelBookingTools hotelBookingTools) {
        this.chatClient = chatClient;
        this.hotelBookingTools = hotelBookingTools;
    }

    public ChatMessageResponse chat(ChatMessageRequest request) {
        String conversationId = (request.conversationId() != null && !request.conversationId().isBlank())
                ? request.conversationId()
                : UUID.randomUUID().toString();

        List<String> toolsUsed = new ArrayList<>();

        if (chatClient != null) {
            try {
                String reply = chatClient.prompt()
                        .user(request.message())
                        .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                        .call()
                        .content();

                return new ChatMessageResponse(reply, conversationId, toolsUsed);
            } catch (Exception e) {
                log.warn("Spring AI LLM call failed or OpenAI API key not configured: {}. Falling back to smart concierge responder.", e.getMessage());
            }
        }

        // Smart Fallback when OpenAI key is absent or offline
        String fallbackReply = generateFallbackResponse(request.message());
        return new ChatMessageResponse(fallbackReply, conversationId, List.of("LocalConciergeAdvisor"));
    }

    private String generateFallbackResponse(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("hotel") || lower.contains("mumbai") || lower.contains("delhi")) {
            var hotels = hotelBookingTools.searchHotelsByCity("");
            return "Welcome to Hotel Booking AI! We found " + hotels.size() + " hotel(s) in our network. " +
                   "You can view available rooms directly or check out our active offers!";
        } else if (lower.contains("offer") || lower.contains("discount") || lower.contains("coupon")) {
            var offers = hotelBookingTools.getActiveOffers();
            return "We currently have " + offers.size() + " active promotion(s)! Try using code 'WELCOME10' for 10% off your booking.";
        }

        return "Hello! I am your Hotel Booking AI Concierge. I can assist you with hotel search, room availability checks, and discount calculations. How can I help you today?";
    }
}
