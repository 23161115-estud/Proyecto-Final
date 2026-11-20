package mx.oaxinvest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import mx.oaxinvest.util.ValidationPatterns;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres") String name,
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            @Size(max = 150, message = "El correo no puede exceder 150 caracteres") String email,
            @NotBlank(message = "La contraseña es obligatoria")
            @Pattern(regexp = ValidationPatterns.PASSWORD, message = ValidationPatterns.PASSWORD_MSG) String password,
            @Pattern(regexp = ValidationPatterns.PHONE, message = ValidationPatterns.PHONE_MSG) String phone) {
    }

    public record LoginRequest(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido") String email,
            @NotBlank(message = "La contraseña es obligatoria") String password) {
    }

    public record ForgotPasswordRequest(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido") String email) {
    }

    public record ResetPasswordRequest(
            @NotBlank(message = "El token es obligatorio") String token,
            @NotBlank(message = "La contraseña es obligatoria")
            @Pattern(regexp = ValidationPatterns.PASSWORD, message = ValidationPatterns.PASSWORD_MSG) String newPassword) {
    }

    public record AuthResponse(String token, String tokenType, long expiresInSeconds, UserDtos.UserResponse user) {
    }

    public record GoogleLoginRequest(
            @NotBlank(message = "El token de Google es obligatorio") String idToken) {
    }

    public record FacebookLoginRequest(
            @NotBlank(message = "El token de Facebook es obligatorio") String accessToken) {
    }

    public record SocialConfigResponse(String googleClientId, String facebookAppId) {
    }
}
