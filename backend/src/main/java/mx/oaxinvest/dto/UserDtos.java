package mx.oaxinvest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import mx.oaxinvest.model.User;
import mx.oaxinvest.util.ValidationPatterns;
import java.time.LocalDateTime;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserResponse(Integer id, String name, String email, String role, String phone, String avatarUrl,
                               boolean emailVerified, boolean active, String provider, LocalDateTime createdAt) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().getName(), u.getPhone(),
                    u.getAvatarUrl(), u.isEmailVerified(), u.isActive(), u.getProvider().name(), u.getCreatedAt());
        }
    }

    public record ProfileUpdateRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres") String name,
            @Pattern(regexp = ValidationPatterns.PHONE, message = ValidationPatterns.PHONE_MSG) String phone) {
    }

    public record ChangePasswordRequest(
            String currentPassword,
            @NotBlank(message = "La nueva contraseña es obligatoria")
            @Pattern(regexp = ValidationPatterns.PASSWORD, message = ValidationPatterns.PASSWORD_MSG) String newPassword) {
    }

    public record UserCreateRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres") String name,
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            @Size(max = 150, message = "El correo no puede exceder 150 caracteres") String email,
            @NotBlank(message = "La contraseña es obligatoria")
            @Pattern(regexp = ValidationPatterns.PASSWORD, message = ValidationPatterns.PASSWORD_MSG) String password,
            @NotBlank(message = "El rol es obligatorio")
            @Pattern(regexp = ValidationPatterns.ROLE, message = ValidationPatterns.ROLE_MSG) String role,
            @Pattern(regexp = ValidationPatterns.PHONE, message = ValidationPatterns.PHONE_MSG) String phone) {
    }

    public record UserUpdateRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres") String name,
            @NotBlank(message = "El rol es obligatorio")
            @Pattern(regexp = ValidationPatterns.ROLE, message = ValidationPatterns.ROLE_MSG) String role,
            @Pattern(regexp = ValidationPatterns.PHONE, message = ValidationPatterns.PHONE_MSG) String phone) {
    }
}
