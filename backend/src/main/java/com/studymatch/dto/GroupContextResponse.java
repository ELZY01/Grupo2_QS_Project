
package com.studymatch.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GroupContextResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime generatedAt,
        List<Integer> generations
) {
    public GroupContextResponse {
        generations = List.copyOf(generations);
    }
}
