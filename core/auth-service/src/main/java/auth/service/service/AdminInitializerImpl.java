package auth.service.service;

import auth.service.model.User;
import auth.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

import static interaction.api.enums.Role.ROLE_ADMIN;
import static interaction.api.enums.Role.ROLE_USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminInitializerImpl implements AdminInitializer {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createAdminIfConfigured(String email, String rawPassword) {
        if (!isConfigured(email, rawPassword)) {
            log.info("Admin bootstrap skipped: app.admin.email/password not configured");
            return;
        }

        String normalizedEmail = normalizeEmail(email);

        Optional<User> existing = userRepository.findWithRolesByEmail(normalizedEmail);
        if (existing.isPresent()) {
            ensureAdminRoles(existing.get(), normalizedEmail);
            return;
        }

        createAdminUser(normalizedEmail, rawPassword);
    }

    private boolean isConfigured(String email, String rawPassword) {
        return email != null && !email.isBlank()
               && rawPassword != null && !rawPassword.isBlank();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void ensureAdminRoles(User user, String normalizedEmail) {
        boolean changed = addAdminRoles(user);

        if (!changed) {
            log.info("Admin already exists: '{}'", normalizedEmail);
            return;
        }

        userRepository.save(user);
        log.info("Admin role granted to existing user '{}'", normalizedEmail);
    }

    private boolean addAdminRoles(User user) {
        boolean changed = user.getRoles().add(ROLE_USER);
        changed |= user.getRoles().add(ROLE_ADMIN);
        return changed;
    }

    private void createAdminUser(String normalizedEmail, String rawPassword) {
        User admin = new User();
        admin.setEmail(normalizedEmail);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setEnabled(true);
        admin.getRoles().add(ROLE_USER);
        admin.getRoles().add(ROLE_ADMIN);

        userRepository.save(admin);
        log.info("Admin user created: '{}'", normalizedEmail);
    }
}