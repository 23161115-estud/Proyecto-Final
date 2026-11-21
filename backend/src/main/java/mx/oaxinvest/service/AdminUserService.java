package mx.oaxinvest.service;

import jakarta.persistence.criteria.Predicate;
import mx.oaxinvest.dto.PageResponse;
import mx.oaxinvest.dto.UserDtos.*;
import mx.oaxinvest.exception.BusinessException;
import mx.oaxinvest.exception.ResourceNotFoundException;
import mx.oaxinvest.model.AuthProvider;
import mx.oaxinvest.model.Role;
import mx.oaxinvest.model.User;
import mx.oaxinvest.repository.RoleRepository;
import mx.oaxinvest.repository.UserRepository;
import mx.oaxinvest.util.PageUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserService {

    private static final Map<String, String> SORT = Map.of("name", "name", "email", "email", "createdAt", "createdAt");

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public AdminUserService(UserRepository users, RoleRepository roles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(String q, String role, Boolean verified, Boolean active,
                                             int page, int size, String sort) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = PageUtil.like(q);
                p.add(cb.or(cb.like(cb.lower(root.get("name")), like, '\\'), cb.like(cb.lower(root.get("email")), like, '\\')));
            }
            if (role != null && !role.isBlank()) {
                p.add(cb.equal(root.get("role").get("name"), role));
            }
            if (verified != null) {
                p.add(cb.equal(root.get("emailVerified"), verified));
            }
            if (active != null) {
                p.add(cb.equal(root.get("active"), active));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        Pageable pageable = PageUtil.of(page, size, sort, SORT, "createdAt");
        return PageResponse.of(users.findAll(spec, pageable).map(UserResponse::from));
    }

    @Transactional(readOnly = true)
    public UserResponse get(Integer id) {
        return UserResponse.from(find(id));
    }

    @Transactional
    public UserResponse create(UserCreateRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Ya existe una cuenta con ese correo");
        }
        User u = new User();
        u.setName(req.name().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        u.setPhone(req.phone());
        u.setProvider(AuthProvider.LOCAL);
        u.setEmailVerified(true);
        u.setRole(role(req.role()));
        return UserResponse.from(users.save(u));
    }

    @Transactional
    public UserResponse update(Integer id, UserUpdateRequest req, Integer currentUserId) {
        User u = find(id);
        if (u.getId().equals(currentUserId) && !u.getRole().getName().equals(req.role())) {
            throw new BusinessException("No puedes cambiar tu propio rol");
        }
        u.setName(req.name().trim());
        u.setPhone(req.phone());
        u.setRole(role(req.role()));
        return UserResponse.from(u);
    }

    @Transactional
    public UserResponse setActive(Integer id, boolean active, Integer currentUserId) {
        User u = find(id);
        if (u.getId().equals(currentUserId) && !active) {
            throw new BusinessException("No puedes desactivar tu propia cuenta");
        }
        u.setActive(active);
        return UserResponse.from(u);
    }

    private User find(Integer id) {
        return users.findWithRoleById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Role role(String name) {
        return roles.findByName(name).orElseThrow(() -> new BusinessException("El rol no existe"));
    }
}
