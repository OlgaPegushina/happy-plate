package order.service.security;

import java.util.UUID;

public interface CurrentUserService {
    UUID userId();

    boolean isAdmin();
}
