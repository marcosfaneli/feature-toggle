package com.fnl33.featuretoggle.service;

import com.fnl33.featuretoggle.domain.Toggle;
import com.fnl33.featuretoggle.repository.ToggleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final ToggleRepository toggleRepository;

    @Transactional(readOnly = true)
    public EvaluationResult evaluate(String toggleName, String value) {
        Toggle toggle = toggleRepository.findByName(toggleName).orElse(null);
        if (toggle == null) {
            return new EvaluationResult(false, "Toggle not found");
        }
        if (!toggle.isEnabled()) {
            return new EvaluationResult(false, "Toggle disabled");
        }
        if (value == null || value.isBlank()) {
            return new EvaluationResult(false, "Value is required for evaluation");
        }
        boolean allowed = toggle.getAllowList().stream()
                .anyMatch(entry -> entry.getValue().equals(value));
        if (allowed) {
            return new EvaluationResult(true, "Value is in allow list");
        }
        return new EvaluationResult(false, "Value not permitted");
    }
}
