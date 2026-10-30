package auth.service.service;

import auth.service.dto.LoginRequest;
import auth.service.dto.RegisterRequest;
import auth.service.dto.TokenResponse;

public interface AuthService {
    void register(RegisterRequest req);

    TokenResponse login(LoginRequest req);
}
