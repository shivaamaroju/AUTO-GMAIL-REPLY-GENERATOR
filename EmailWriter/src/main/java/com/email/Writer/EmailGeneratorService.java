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

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

String uri = UriComponentsBuilder.fromHttpUrl(geminiApiUrl)
        .queryParam("key", geminiApiKey)
        .build()
        .toUriString();

        logger.info("Sending request to: {}", uri);  // Log the full URI

        try {
            String response = webClient.post()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.debug("Raw API Response: {}", response); //Log the full Response

            return extractResponseContent(response);

        } catch (Exception e) {
            logger.error("Error during API call: {}", e.getMessage(), e);  // Log the exception
            return "Error during API call: " + e.getMessage(); // Return error message
        }
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            // More robust path traversal with null checks
            JsonNode candidates = rootNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        JsonNode firstPart = parts.get(0);
                        if (firstPart != null && firstPart.has("text")) {
                            return firstPart.get("text").asText();
                        } else {
                            logger.warn("Response missing 'text' field.");
                            return "Error: Response missing 'text' field.";
                        }
                    } else {
                        logger.warn("Response missing 'parts' array.");
                        return "Error: Response missing 'parts' array.";
                    }
                } else {
                    logger.warn("Response missing 'content' field.");
                    return "Error: Response missing 'content' field.";
                }
            } else {
                logger.warn("Response missing 'candidates' array.");
                return "Error: Response missing 'candidates' array.";
            }

        } catch (Exception e) {
            logger.error("Error processing response: {}", e.getMessage(), e);
            return "Error Processing Response: " + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email content. Please Don't generate a subject Line:");

        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append(" Use a ").append(emailRequest.getTone()).append(" Tone.");
        }

        prompt.append("\nOriginal email content:\n").append(emailRequest.getEmailContent());

        logger.debug("Generated Prompt: {}", prompt.toString());  // Log the prompt

        return prompt.toString();
    }

    @Configuration
    public static class WebClientConfig {

        @Bean
        public WebClient webClient() {
            return WebClient.builder()
                    .build();
        }
    }
}
