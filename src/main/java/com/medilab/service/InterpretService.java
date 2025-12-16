package com.medilab.service;

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

    @Value("${google.api.key}")
    private String googleApiKey;

    @Value("${google.ai.model:gemini-1.5-flash}")
    private String modelName;

    private final RestClient restClient = RestClient.create();

    public String getInterpretation(String testName, String resultValue) {
        if ("placeholder".equalsIgnoreCase(googleApiKey) || googleApiKey == null || googleApiKey.isEmpty()) {
            log.warn("Google API Key is not configured. Returning placeholder response.");
            return "AI Interpretation is not configured. Please set a valid Google API Key in the backend application.properties.";
        }

        String prompt = String.format(
                "Interpret the following lab test result: %s, value: %s. Provide a concise summary of what this indicates.",
                testName, resultValue);

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)))));

            Map response = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/" + modelName
                            + ":generateContent?key=" + googleApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("candidates")) {
                List candidates = (List) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map firstCandidate = (Map) candidates.get(0);
                    Map content = (Map) firstCandidate.get("content");
                    List parts = (List) content.get("parts");
                    if (!parts.isEmpty()) {
                        Map firstPart = (Map) parts.get(0);
                        return (String) firstPart.get("text");
                    }
                }
            }
            return "Could not generate interpretation.";

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return "Error retrieving AI interpretation: " + e.getMessage();
        }
    }
}
