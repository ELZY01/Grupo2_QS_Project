
package com.studymatch.dto;

import java.util.List;

public record CreateGroupContextRequest(
        String name,
        List<Long> studentIds
) {
}
