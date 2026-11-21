package mx.oaxinvest.service;

import com.fasterxml.jackson.databind.JsonNode;
import mx.oaxinvest.dto.AuthDtos.AuthResponse;
import mx.oaxinvest.dto.AuthDtos.SocialConfigResponse;
import mx.oaxinvest.dto.UserDtos.UserResponse;
import mx.oaxinvest.exception.BusinessException;
import mx.oaxinvest.exception.ExternalServiceException;
import mx.oaxinvest.model.AuthProvider;
import mx.oaxinvest.model.User;
import mx.oaxinvest.repository.RoleRepository;
import mx.oaxinvest.repository.UserRepository;
import mx.oaxinvest.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Set;

@Service
public class SocialAuthService {

    private static final Logger log = LoggerFactory.getLogger(SocialAuthService.class);
    private static final Set<String> GOOGLE_ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final UserRepository users;
    private final RoleRepository roles;
    private final JwtService jwt;
    private final String googleClientId;
    private final String facebookAppId;
    private final String facebookAppSecret;
    private final RestClient http;

    public SocialAuthService(UserRepository users, RoleRepository roles, JwtService jwt,
                             @Value("${app.google.client-id:}") String googleClientId,
                             @Value("${app.facebook.app-id:}") String facebookAppId,
                             @Value("${app.facebook.app-secret:}") String facebookAppSecret) {
        this.users = users;
        this.roles = roles;
        this.jwt = jwt;
        this.googleClientId = googleClientId.trim();
        this.facebookAppId = facebookAppId.trim();
        this.facebookAppSecret = facebookAppSecret.trim();
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000);
        f.setReadTimeout(10000);
        this.http = RestClient.builder().requestFactory(f).build();
    }

    public SocialConfigResponse publicConfig() {
        return new SocialConfigResponse(googleClientId, facebookAppId.isEmpty() || facebookAppSecret.isEmpty() ? "" : facebookAppId);
    }

    public AuthResponse loginWithGoogle(String idToken) {
        if (googleClientId.isEmpty()) {
            throw new BusinessException("El inicio de sesión con Google no está configurado en el servidor");
        }
        JsonNode info = getJson("https://oauth2.googleapis.com/tokeninfo?id_token={t}", "Google", idToken);
        String aud = info.path("aud").asText("");
        String iss = info.path("iss").asText("");
        String email = info.path("email").asText("");
        boolean verified = "true".equalsIgnoreCase(info.path("email_verified").asText("false"));
        if (!googleClientId.equals(aud) || !GOOGLE_ISSUERS.contains(iss) || email.isBlank() || !verified) {
            throw new BadCredentialsException("Token de Google no válido");
        }
        String name = info.path("name").asText("");
        return finishLogin(email, displayName(name, email), AuthProvider.GOOGLE);
    }

    public AuthResponse loginWithFacebook(String accessToken) {
        if (facebookAppId.isEmpty() || facebookAppSecret.isEmpty()) {
            throw new BusinessException("El inicio de sesión con Facebook no está configurado en el servidor");
        }
        JsonNode debug = getJson("https://graph.facebook.com/debug_token?input_token={t}&access_token={app}",
                "Facebook", accessToken, facebookAppId + "|" + facebookAppSecret).path("data");
        if (!debug.path("is_valid").asBoolean(false) || !facebookAppId.equals(debug.path("app_id").asText(""))) {
            throw new BadCredentialsException("Token de Facebook no válido");
        }
        JsonNode me = getJson("https://graph.facebook.com/me?fields=id,name,email&access_token={t}", "Facebook", accessToken);
        String email = me.path("email").asText("");
        if (email.isBlank()) {
            throw new BusinessException(
                    "Tu cuenta de Facebook no comparte un correo. Usa otro método de inicio de sesión o regístrate con correo");
        }
        String name = me.path("name").asText("");
        return finishLogin(email, displayName(name, email), AuthProvider.FACEBOOK);
    }

    private AuthResponse finishLogin(String rawEmail, String rawName, AuthProvider provider) {
        String email = rawEmail.trim().toLowerCase();
        User user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setName(clip(rawName.trim(), 100));
            user.setEmail(clip(email, 150));
            user.setProvider(provider);
            user.setPasswordHash(null);
            user.setEmailVerified(true);
            user.setRole(roles.findByName("CLIENTE").orElseThrow(() -> new IllegalStateException("Falta el rol CLIENTE")));
            user = users.save(user);
            log.info("Usuario nuevo por {}: {}", provider, email);
        } else {
            if (!user.isActive()) {
                throw new BadCredentialsException("Cuenta desactivada");
            }
            if (user.getProvider() == AuthProvider.LOCAL && !user.isEmailVerified()) {
                user.setPasswordHash(null);
                user.setProvider(provider);
                user.setVerificationToken(null);
            }
            user.setEmailVerified(true);
            user = users.save(user);
        }
        return new AuthResponse(jwt.generate(user), "Bearer", jwt.getExpirationSeconds(), UserResponse.from(user));
    }

    private JsonNode getJson(String uri, String providerName, Object... vars) {
        try {
            JsonNode body = http.get().uri(uri, vars).retrieve().body(JsonNode.class);
            if (body == null) {
                throw new BadCredentialsException("Respuesta vacía de " + providerName);
            }
            return body;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new BadCredentialsException("Token de " + providerName + " no válido");
            }
            log.warn("{} respondió {}", providerName, e.getStatusCode());
            throw new ExternalServiceException("No se pudo verificar tu cuenta con " + providerName + ". Intenta de nuevo");
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Fallo al contactar a {}: {}", providerName, e.getMessage());
            throw new ExternalServiceException("No se pudo contactar a " + providerName + ". Intenta de nuevo en unos minutos");
        }
    }

    private static String displayName(String name, String email) {
        String n = name == null ? "" : name.trim();
        if (n.length() >= 2) {
            return n;
        }
        int at = email.indexOf('@');
        String local = at > 0 ? email.substring(0, at) : email;
        return local.length() >= 2 ? local : local + "_";
    }

    private static String clip(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
