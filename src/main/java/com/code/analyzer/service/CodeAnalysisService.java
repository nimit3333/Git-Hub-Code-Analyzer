package com.code.analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
public class CodeAnalysisService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    public CodeAnalysisService(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String analyzeCode(String code) {

        String prompt = buildPrompt(code);

        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        try {

            String response = webClient.post()
                    .uri("/v1beta/models/gemini-3-flash-preview:generateContent")
                    .header("x-goog-api-key", apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(6)) // ✅ prevent hanging
                    .block();

            return extractText(response);

        } catch (Exception e) {
            return "AI service unavailable. Please try again.";
        }
    }

    private String extractText(String response) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            if (root.path("candidates").isEmpty()) {
                return "No response from AI";
            }

            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            return "Error processing AI response";
        }
    }

    private String buildPrompt(String code) {

        // ✅ Basic sanitization
        String safeCode = code
                .replaceAll("(?i)ignore instructions", "")
                .replaceAll("(?i)system prompt", "");

        return """
        Analyze the following code.

        Provide:

        1. Simple explanation of what the code does
        2. Step-by-step logic
        3. Identify bugs or bad practices
        4. Suggest improvements
        5. Suggest performance optimizations

        Instructions:
        - Do NOT use # or *
        - Use clean structured points
        - Highlight headings clearly
        - Keep output easy to read

        Code:
        """ + safeCode;
    }
}