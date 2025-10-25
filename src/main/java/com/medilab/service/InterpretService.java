package com.medilab.service;

import org.springframework.stereotype.Service;

@Service
public class InterpretService {

    public String getInterpretation(String testName, String resultValue) {
        // In a real application, you would call the Gemini API here.
        return "This is a simplified interpretation for " + testName + " with a result of " + resultValue;
    }
}
