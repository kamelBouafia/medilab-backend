package com.medilab.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterpretService {

    @Value("${openrouter.api.key}")
    private String openrouterApiKey;

    @Value("${openrouter.site.url}")
    private String openrouterSiteUrl;

    private final RestClient restClient = RestClient.create();

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatCompletionResponse {
        public List<Choice> choices;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Choice {
        public Message message;
        @JsonProperty("finish_reason")
        public String finishReason;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Message {
        public String content;
    }

    public String getInterpretation(String testName, String resultValue, String language) {
        if ("placeholder".equalsIgnoreCase(openrouterApiKey) || openrouterApiKey == null || openrouterApiKey.isEmpty()) {
            log.warn("OpenRouter API Key is not configured. Returning placeholder response.");
            return "AI Interpretation is not configured. Please set a valid OpenRouter API Key in the backend application.properties.";
        }

        String prompt = String.format(
                "Interpret the following lab test result: %s, value: %s. Provide a one-sentence summary of what this indicates in '%s' language.",
                testName, resultValue, language);

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "qwen/qwen-2.5-7b-instruct",
                    "messages", List.of(
                            Map.of("role", "user",
                                    "content", prompt)));

            ChatCompletionResponse response = restClient.post()
                    .uri(openrouterSiteUrl)
                    .header("Authorization", "Bearer " + openrouterApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            if (response != null && response.choices != null && !response.choices.isEmpty()) {
                Choice firstChoice = response.choices.getFirst();
                if (firstChoice != null) {
                    if ("length".equalsIgnoreCase(firstChoice.finishReason)) {
                        log.warn("AI interpretation may be truncated. Finish reason: length. Content: {}", firstChoice.message.content);
                    }
                    if (firstChoice.message != null && firstChoice.message.content != null) {
                        String content = firstChoice.message.content.trim();
                        if (content.startsWith("\"") && content.endsWith("\"")) {
                            content = content.substring(1, content.length() - 1);
                        }
                        return content;
                    }
                }
            }
            return "Could not generate interpretation.";

        } catch (Exception e) {
            log.error("Error calling OpenRouter API", e);
            return "Error retrieving AI interpretation: " + e.getMessage();
        }
    }
}
