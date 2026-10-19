package com.studymatch.controller;

import com.studymatch.dto.ProfileResponse;
import com.studymatch.service.ProfileService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfil de competências de um estudante (Issue #45, UC07 e US16).
 */
@RestController
@RequestMapping("/api/students/{studentId}/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // Calcula o perfil com os dados atuais: uma nota nova altera logo o resultado.
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long studentId) {
        return ResponseEntity.ok(profileService.getProfile(studentId));
    }
}
