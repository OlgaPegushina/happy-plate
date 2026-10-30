package auth.service.service;

import auth.service.dto.LoginRequest;
import auth.service.dto.RegisterRequest;
import auth.service.dto.TokenResponse;
import auth.service.exception.BadCredentialsException;
import auth.service.exception.EmailAlreadyExistsException;
import auth.service.model.User;
import auth.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

import static interaction.api.enums.Role.ROLE_USER;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Transactional
    @Override
    public void register(RegisterRequest req) {
        String email = normalizeEmail(req.email());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);

        // -- В базе хранится не пароль, а хэш пароля
        user.setPasswordHash(passwordEncoder.encode(req.password()));

        // -- Флаг активности пользователя
        user.setEnabled(true);

        // -- Роль по умолчанию для нового пользователя
        user.getRoles().add(ROLE_USER);

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public TokenResponse login(LoginRequest req) {
        String email = normalizeEmail(req.email());

        User user = userRepository.findWithRolesByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        // -- Проверяем, что пользователь активен и пароль совпадает
        if (!user.isEnabled() || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Bad credentials");
        }

        // -- время жизни access token в секундах
        long ttl = 900;

        // --Генерируем JWT с ролями и нужными claims
        String accessToken = jwtService.issueAccessToken(user, ttl);

        return new TokenResponse(accessToken, null, ttl);
    }

    private String normalizeEmail(String rawEmail) {
        return rawEmail == null ? null : rawEmail.trim().toLowerCase(Locale.ROOT);
    }
}