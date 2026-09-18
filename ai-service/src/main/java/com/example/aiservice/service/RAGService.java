package com.example.aiservice.service;

import com.example.aiservice.client.HotelInventoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RAGService {

    private static final Logger log = LoggerFactory.getLogger(RAGService.class);

    private final VectorStore vectorStore;
    private final HotelInventoryClient hotelInventoryClient;

    public RAGService(
            @Autowired(required = false) VectorStore vectorStore,
            HotelInventoryClient hotelInventoryClient) {
        this.vectorStore = vectorStore;
        this.hotelInventoryClient = hotelInventoryClient;
    }

    public int indexAllHotels() {
        if (vectorStore == null) {
            log.warn("VectorStore is not configured or disabled.");
            return 0;
        }

        try {
            List<Map<String, Object>> hotels = hotelInventoryClient.getHotels(null);
            if (hotels == null || hotels.isEmpty()) {
                log.info("No hotels found to index.");
                return 0;
            }

            List<Document> documents = new ArrayList<>();
            for (Map<String, Object> hotel : hotels) {
                Long id = ((Number) hotel.get("id")).longValue();
                String name = (String) hotel.get("name");
                String city = (String) hotel.get("city");
                String address = (String) hotel.get("address");
                String description = (String) hotel.get("description");

                String text = String.format(
                        "Hotel: %s. City: %s. Address: %s. Description: %s",
                        name, city, address, description
                );

                Map<String, Object> metadata = Map.of(
                        "hotelId", id,
                        "name", name,
                        "city", city != null ? city : ""
                );

                documents.add(new Document(text, metadata));
            }

            vectorStore.add(documents);
            log.info("Indexed {} hotels into VectorStore", documents.size());
            return documents.size();
        } catch (Exception e) {
            log.error("Failed to index hotels into VectorStore: {}", e.getMessage());
            return 0;
        }
    }

    public List<Map<String, Object>> search(String query, int topK) {
        if (vectorStore == null) {
            log.warn("VectorStore is not available for similarity search.");
            return Collections.emptyList();
        }

        try {
            List<Document> matchedDocs = vectorStore.similaritySearch(
                    SearchRequest.builder().query(query).topK(topK).build()
            );

            List<Map<String, Object>> results = new ArrayList<>();
            for (Document doc : matchedDocs) {
                results.add(Map.of(
                        "content", doc.getText(),
                        "metadata", doc.getMetadata()
                ));
            }
            return results;
        } catch (Exception e) {
            log.warn("VectorStore similarity search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
