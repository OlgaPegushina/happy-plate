package order.service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static interaction.api.utility.AppConstants.CLAIM_USER_ID;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    @Override
    public UUID userId() {
        var jwt = jwtToken().getToken();

        String raw = jwt.getClaimAsString(CLAIM_USER_ID);
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("JWT claim '" + CLAIM_USER_ID + "' is missing");
        }
        return UUID.fromString(raw);
    }

    @Override
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private JwtAuthenticationToken jwtToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken token) {
            return token;
        }
        throw new IllegalStateException("Authentication is not JwtAuthenticationToken: " +
                                        (auth == null ? "null" : auth.getClass().getName()));
    }
}