package mx.oaxinvest.controller;

import jakarta.validation.Valid;
import mx.oaxinvest.dto.AuthDtos.*;
import mx.oaxinvest.dto.MessageResponse;
import mx.oaxinvest.service.AuthService;
import mx.oaxinvest.service.SocialAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final SocialAuthService social;

    public AuthController(AuthService auth, SocialAuthService social) {
        this.auth = auth;
        this.social = social;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(req));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @PostMapping("/google")
    public AuthResponse google(@Valid @RequestBody GoogleLoginRequest req) {
        return social.loginWithGoogle(req.idToken());
    }

    @PostMapping("/facebook")
    public AuthResponse facebook(@Valid @RequestBody FacebookLoginRequest req) {
        return social.loginWithFacebook(req.accessToken());
    }

    @GetMapping("/social-config")
    public SocialConfigResponse socialConfig() {
        return social.publicConfig();
    }

    @PostMapping("/logout")
    public MessageResponse logout(@RequestHeader(value = "Authorization", required = false) String header) {
        return auth.logout(header);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgot(@Valid @RequestBody ForgotPasswordRequest req) {
        return auth.forgotPassword(req);
    }

    @PostMapping("/reset-password")
    public MessageResponse reset(@Valid @RequestBody ResetPasswordRequest req) {
        return auth.resetPassword(req);
    }

    @GetMapping("/verify-email")
    public MessageResponse verify(@RequestParam String token) {
        return auth.verifyEmail(token);
    }
}
