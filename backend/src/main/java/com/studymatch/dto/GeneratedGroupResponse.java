
package com.studymatch.dto;

import java.util.List;
import java.util.Map;

public record GeneratedGroupResponse(
        Long id,
        Integer generationNumber,
        Integer groupNumber,
        Double average,
        Double gapToOthers,
        List<GroupStudentResponse> members
) {
    public GeneratedGroupResponse {
        members = List.copyOf(members);
    }

    public record GroupStudentResponse(
            Long studentId,
            String studentNumber,
            String name
    ) {
    }
}
