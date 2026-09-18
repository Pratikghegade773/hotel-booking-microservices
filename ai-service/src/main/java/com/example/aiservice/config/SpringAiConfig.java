package com.example.aiservice.config;

import com.example.aiservice.tools.HotelBookingTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiConfig {

    private static final String SYSTEM_PROMPT = """
            You are the intelligent AI Concierge for the Hotel Booking AI platform.
            Your role is to assist guests in:
            1. Searching for hotels by city or amenities.
            2. Checking room details, capacity, and current availability.
            3. Calculating discounts and explaining promotional offers.
            4. Guiding guests through the booking process.
            
            Always use your available tools to look up real-time hotel, room, and discount information.
            Be warm, professional, accurate, and concise.
            """;

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public ChatClient chatClient(
            @Autowired(required = false) ChatModel chatModel,
            ChatMemory chatMemory,
            HotelBookingTools hotelBookingTools) {
        if (chatModel == null) {
            return null;
        }

        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(hotelBookingTools)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }
}
