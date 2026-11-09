package com.studymatch.config;

import com.studymatch.model.Role;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.AuthenticatedUser;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenAuthenticationFilter;
import com.studymatch.security.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Autenticação e permissões do backend.
 *
 * Login com token (POST /api/auth/login) e regras por role:
 * - ADMIN: gestão de estudantes, notas, UCs e atribuições (/api/admin/**).
 * - DOCENTE: a sua área (/api/teacher/**) e os alunos das suas UCs.
 * - ALUNO: só os seus próprios dados.
 * Sem sessão → 401. Com sessão mas sem permissão → 403.
 */
@Configuration
public class SecurityConfig {

    private static final String STUDENT = "/api/students/{studentId}";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   TokenService tokenService,
                                                   AppUserRepository userRepository,
                                                   StudentAccess access) throws Exception {
        String admin = Role.ADMIN.name();
        String teacher = Role.DOCENTE.name();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(new TokenAuthenticationFilter(tokenService, userRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                writeError(res, HttpServletResponse.SC_UNAUTHORIZED, "Tens de iniciar sessão."))
                        .accessDeniedHandler((req, res, ex) ->
                                writeError(res, HttpServletResponse.SC_FORBIDDEN, "Não tens permissão para isto.")))
                .authorizeHttpRequests(auth -> auth
                        // Públicos
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()

                        // Áreas por role
                        .requestMatchers("/api/admin/**").hasRole(admin)
                        .requestMatchers("/api/teacher/**").hasRole(teacher)
                        .requestMatchers("/api/group-contexts/**").hasAnyRole(admin, teacher)

                        // Estudantes
                        .requestMatchers(HttpMethod.GET, "/api/students").hasRole(admin)
                        .requestMatchers(HttpMethod.POST, "/api/students").hasRole(admin)
                        .requestMatchers(HttpMethod.PUT, STUDENT).access(student(access::canEdit))
                        .requestMatchers(HttpMethod.POST, STUDENT + "/enrollments").hasRole(admin)
                        .requestMatchers(HttpMethod.POST, STUDENT + "/quiz-submissions").access(student(access::isSelf))
                        .requestMatchers(HttpMethod.GET, STUDENT, STUDENT + "/**").access(student(access::canView))

                        // Resto (quiz, UCs...): basta ter sessão
                        .anyRequest().authenticated());

        return http.build();
    }

    /**
     * Decide com base no {studentId} do URL e no utilizador autenticado.
     */
    private static AuthorizationManager<RequestAuthorizationContext> student(
            BiPredicate<AuthenticatedUser, Long> rule) {
        return (authentication, context) -> {
            AuthenticatedUser user = StudentAccess.currentUser(authentication.get());
            Long studentId = parseId(context.getVariables().get("studentId"));
            return new AuthorizationDecision(rule.test(user, studentId));
        };
    }

    private static Long parseId(String value) {
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Contas vêm da tabela app_users (evita a conta por omissão do Spring Boot).
     */
    @Bean
    public UserDetailsService userDetailsService(AppUserRepository userRepository) {
        return username -> userRepository.findByUsernameIgnoreCase(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPasswordHash())
                        .roles(u.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    /**
     * Mesmas regras de CORS do WebConfig, agora também para os pedidos com Authorization.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
