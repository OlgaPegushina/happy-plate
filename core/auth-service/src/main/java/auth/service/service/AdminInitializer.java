package auth.service.service;

public interface AdminInitializer {
    void createAdminIfConfigured(String email, String password);
}
