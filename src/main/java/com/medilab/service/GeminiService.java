package com.medilab.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class GeminiService {

    private final String apiKey;
    private final RestTemplate rest = new RestTemplate();

    public GeminiService(@Value("${app.gemini-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    public Map<String, Object> interpret(String testName, String resultValue) {
        // Stub implementation for development: returns a simple interpretation.
        // Replace with real Gemini call using apiKey in production.
        String interpretation = "[AUTO] " + testName + " -> " + resultValue + ". This is a stubbed interpretation.";
        return Map.of("interpretation", interpretation);
    }
}
