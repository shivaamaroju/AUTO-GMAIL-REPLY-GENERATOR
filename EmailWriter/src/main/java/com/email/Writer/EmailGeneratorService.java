package com.email.Writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;
import java.util.Map;

@Service
public class EmailGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(EmailGeneratorService.class);
    private final WebClient webClient;

    @Value("${GEMINI_API_URL}")
    private String geminiApiUrl;

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        String prompt = buildPrompt(emailRequest);

        // Standardized Gemini JSON structure
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        String uri = UriComponentsBuilder.fromHttpUrl(geminiApiUrl)
                .queryParam("key", geminiApiKey)
                .build()
                .toUriString();

        try {
            return webClient.post()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(this::extractResponseContent)
                    .block();
        } catch (Exception e) {
            logger.error("API Error: {}", e.getMessage());
            return "Error during API call: " + e.getMessage();
        }
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            return "Error processing response: " + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        return "Generate a professional email reply for the following email content. No subject line. " 
             + (emailRequest.getTone() != null ? "Tone: " + emailRequest.getTone() : "") 
             + "\n\nContent: " + emailRequest.getEmailContent();
    }

    @Configuration
    public static class WebClientConfig {
        @Bean
        public WebClient webClient() {
            return WebClient.builder().build();
        }
    }
}
