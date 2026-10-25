package auth.service.dto;

public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {
}
