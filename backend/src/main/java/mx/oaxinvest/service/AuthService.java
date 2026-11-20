package mx.oaxinvest.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import mx.oaxinvest.dto.AuthDtos.*;
import mx.oaxinvest.dto.MessageResponse;
import mx.oaxinvest.dto.UserDtos.UserResponse;
import mx.oaxinvest.exception.BusinessException;
import mx.oaxinvest.model.*;
import mx.oaxinvest.repository.*;
import mx.oaxinvest.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordResetTokenRepository resetTokens;
    private final RevokedTokenRepository revoked;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final MailService mail;
    private final boolean requireVerified;

    public AuthService(UserRepository users, RoleRepository roles, PasswordResetTokenRepository resetTokens,
                       RevokedTokenRepository revoked, PasswordEncoder encoder, JwtService jwt, MailService mail,
                       @Value("${app.auth.require-email-verified:false}") boolean requireVerified) {
        this.users = users;
        this.roles = roles;
        this.resetTokens = resetTokens;
        this.revoked = revoked;
        this.encoder = encoder;
        this.jwt = jwt;
        this.mail = mail;
        this.requireVerified = requireVerified;
    }

    public MessageResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Ya existe una cuenta con ese correo");
        }
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(req.password()));
        user.setPhone(req.phone());
        user.setProvider(AuthProvider.LOCAL);
        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setRole(roles.findByName("CLIENTE").orElseThrow(() -> new IllegalStateException("Falta el rol CLIENTE")));
        user = users.save(user);
        mail.sendVerification(user);
        return new MessageResponse("Cuenta creada. Revisa tu correo para confirmarla");
    }

    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email().trim()).orElse(null);
        if (user == null || user.getPasswordHash() == null || !user.isActive()
                || !encoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        if (requireVerified && !user.isEmailVerified()) {
            throw new BusinessException("Confirma tu correo antes de iniciar sesión");
        }
        return new AuthResponse(jwt.generate(user), "Bearer", jwt.getExpirationSeconds(), UserResponse.from(user));
    }

    public MessageResponse logout(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                Claims c = jwt.parse(authorizationHeader.substring(7));
                RevokedToken r = new RevokedToken();
                r.setJti(c.getId());
                r.setExpiresAt(LocalDateTime.ofInstant(c.getExpiration().toInstant(), ZoneId.systemDefault()));
                revoked.save(r);
            } catch (JwtException | IllegalArgumentException ignored) {
            }
        }
        return new MessageResponse("Sesión cerrada");
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        User user = users.findByVerificationToken(token)
                .orElseThrow(() -> new BusinessException("El enlace de verificación no es válido o ya se usó"));
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        mail.sendWelcome(user);
        return new MessageResponse("Correo confirmado. Ya puedes iniciar sesión");
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest req) {
        users.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> u.getProvider() == AuthProvider.LOCAL && u.isActive())
                .ifPresent(u -> {
                    PasswordResetToken t = new PasswordResetToken();
                    t.setUserId(u.getId());
                    t.setToken(UUID.randomUUID().toString());
                    t.setExpiresAt(LocalDateTime.now().plusMinutes(30));
                    resetTokens.save(t);
                    mail.sendPasswordReset(u, t.getToken());
                });
        return new MessageResponse("Si el correo existe, recibirás un enlace para cambiar tu contraseña");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest req) {
        PasswordResetToken t = resetTokens.findByToken(req.token())
                .filter(x -> !x.isUsed() && x.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new BusinessException("El enlace no es válido o ya venció. Solicita uno nuevo"));
        User user = users.findById(t.getUserId()).orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        user.setPasswordHash(encoder.encode(req.newPassword()));
        t.setUsed(true);
        return new MessageResponse("Contraseña actualizada. Ya puedes iniciar sesión");
    }
}
