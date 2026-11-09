package com.studymatch.config;

import com.studymatch.model.AppUser;
import com.studymatch.model.Role;
import com.studymatch.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria as contas de demonstração (admin e 1 docente) se ainda não existirem.
 * Nunca altera contas que já existem.
 *
 * Credenciais por variáveis de ambiente: ADMIN_USER, ADMIN_PASSWORD, DOCENTE_USER, DOCENTE_PASSWORD.
 * Desligar com studymatch.seed.enabled=false.
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "studymatch.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUser;
    private final String adminPassword;
    private final String teacherUser;
    private final String teacherPassword;
    private final String teacherName;

    public DataSeeder(AppUserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${studymatch.seed.admin.username:admin}") String adminUser,
                      @Value("${studymatch.seed.admin.password:studymatch}") String adminPassword,
                      @Value("${studymatch.seed.teacher.username:docente}") String teacherUser,
                      @Value("${studymatch.seed.teacher.password:docente123}") String teacherPassword,
                      @Value("${studymatch.seed.teacher.name:Docente de Teste}") String teacherName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.teacherUser = teacherUser;
        this.teacherPassword = teacherPassword;
        this.teacherName = teacherName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensure(adminUser, adminPassword, Role.ADMIN, "Administrador");
        ensure(teacherUser, teacherPassword, Role.DOCENTE, teacherName);
    }

    private void ensure(String username, String password, Role role, String name) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }
        userRepository.save(new AppUser(username, passwordEncoder.encode(password), role, name, null));
        log.info("Conta de demonstração criada: {} ({})", username, role);
    }
}
