package com.studymatch.config;

import com.studymatch.model.AppUser;
import com.studymatch.model.Role;
import com.studymatch.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitários das contas carregadas pelo Spring Security (tabela app_users).
 */
class SecurityConfigTest {

    private final AppUserRepository users = mock(AppUserRepository.class);
    private final UserDetailsService service = new SecurityConfig().userDetailsService(users);

    @Test
    void loadsTheAccountWithItsHashAndRole() {
        when(users.findByUsernameIgnoreCase("prof"))
                .thenReturn(Optional.of(new AppUser("prof", "hash", Role.DOCENTE, "Prof. Rita", null)));

        UserDetails details = service.loadUserByUsername("prof");

        assertEquals("prof", details.getUsername());
        assertEquals("hash", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> "ROLE_DOCENTE".equals(a.getAuthority())));
    }

    @Test
    void anUnknownUsernameIsNotFound() {
        when(users.findByUsernameIgnoreCase("ninguem")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ninguem"));
    }
}
