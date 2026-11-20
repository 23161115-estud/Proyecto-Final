package mx.oaxinvest.security;

import mx.oaxinvest.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AccessDeniedException("No autenticado");
        }
        return details.getUser();
    }

    public static boolean isAdmin(User u) {
        return "ADMIN".equals(u.getRole().getName());
    }
}
