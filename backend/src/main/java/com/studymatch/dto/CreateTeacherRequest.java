package com.studymatch.dto;

/**
 * Nova conta de docente, criada pelo admin.
 */
public record CreateTeacherRequest(String username, String displayName, String password) {
}
