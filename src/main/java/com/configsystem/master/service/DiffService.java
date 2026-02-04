package com.configsystem.master.service;

import com.configsystem.master.dto.ConfigInstruction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DiffService {

    public List<ConfigInstruction> calculateInstructions(Map<String, String> current, Map<String, String> target) {
        List<ConfigInstruction> instructions = new ArrayList<>();

        // 1. Identify ADD and UPDATE
        target.forEach((key, targetValue) -> {
            if (!current.containsKey(key)) {
                // Key is in target but not in current -> ADD
                instructions.add(new ConfigInstruction("ADD", key, targetValue));
            } else if (!Objects.equals(current.get(key), targetValue)) {
                // Key exists in both but values differ -> UPDATE
                instructions.add(new ConfigInstruction("UPDATE", key, targetValue));
            }
        });

        // 2. Identify DELETE
        current.keySet().forEach(key -> {
            if (!target.containsKey(key)) {
                // Key is in current but removed in target -> DELETE
                instructions.add(new ConfigInstruction("DELETE", key, null));
            }
        });

        return instructions;
    }
}