package mx.oaxinvest.service;

import mx.oaxinvest.dto.MessageResponse;
import mx.oaxinvest.dto.UserDtos.*;
import mx.oaxinvest.exception.BusinessException;
import mx.oaxinvest.exception.ResourceNotFoundException;
import mx.oaxinvest.model.User;
import mx.oaxinvest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class ProfileService {

    private static final Map<String, String> TYPES = Map.of("image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String uploadDir;

    public ProfileService(UserRepository users, PasswordEncoder encoder, @Value("${app.upload-dir}") String uploadDir) {
        this.users = users;
        this.encoder = encoder;
        this.uploadDir = uploadDir;
    }

    private User load(Integer id) {
        return users.findWithRoleById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public UserResponse get(Integer id) {
        return UserResponse.from(load(id));
    }

    @Transactional
    public UserResponse update(Integer id, ProfileUpdateRequest req) {
        User u = load(id);
        u.setName(req.name().trim());
        u.setPhone(req.phone());
        return UserResponse.from(u);
    }

    @Transactional
    public MessageResponse changePassword(Integer id, ChangePasswordRequest req) {
        User u = load(id);
        if (u.getPasswordHash() != null) {
            if (req.currentPassword() == null || !encoder.matches(req.currentPassword(), u.getPasswordHash())) {
                throw new BusinessException("La contraseña actual no es correcta");
            }
        }
        u.setPasswordHash(encoder.encode(req.newPassword()));
        return new MessageResponse("Contraseña actualizada");
    }

    @Transactional
    public UserResponse uploadAvatar(Integer id, MultipartFile file) {
        String ext = file == null ? null : TYPES.get(file.getContentType());
        if (file == null || file.isEmpty() || ext == null) {
            throw new BusinessException("Sube una imagen JPG, PNG o WEBP");
        }
        try {
            Path dir = Path.of(uploadDir, "avatars");
            Files.createDirectories(dir);
            String name = id + "-" + UUID.randomUUID() + "." + ext;
            Files.copy(file.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            User u = load(id);
            u.setAvatarUrl("/uploads/avatars/" + name);
            return UserResponse.from(u);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen", e);
        }
    }

    @Transactional
    public MessageResponse deleteAccount(Integer id) {
        User u = load(id);
        if ("ADMIN".equals(u.getRole().getName())) {
            throw new BusinessException("La cuenta de administrador no se puede eliminar");
        }
        users.delete(u);
        return new MessageResponse("Cuenta eliminada");
    }
}
