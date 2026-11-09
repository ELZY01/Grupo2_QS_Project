package com.studymatch.dto;

import com.studymatch.model.AppUser;

/**
 * Docente devolvido pela API (sem dados da palavra-passe).
 */
public record TeacherResponse(Long id, String username, String displayName) {

    public static TeacherResponse from(AppUser user) {
        return new TeacherResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
