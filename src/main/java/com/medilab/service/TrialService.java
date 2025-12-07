package com.medilab.service;

import com.medilab.entity.Lab;
import com.medilab.exception.TrialExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrialService {

    public void assertTrialActive(Lab lab) {
        if (lab == null) return; // defensive; other validations happen elsewhere
        LocalDateTime now = LocalDateTime.now();
        if (lab.getTrialEnd() != null && now.isAfter(lab.getTrialEnd())) {
            throw new TrialExpiredException("Trial expired. Please upgrade to continue using this lab.");
        }
    }
}

