package mx.oaxinvest.controller;

import jakarta.validation.Valid;
import mx.oaxinvest.dto.PageResponse;
import mx.oaxinvest.dto.UserDtos.*;
import mx.oaxinvest.security.CurrentUser;
import mx.oaxinvest.service.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<UserResponse> list(@RequestParam(required = false) String q,
                                           @RequestParam(required = false) String role,
                                           @RequestParam(required = false) Boolean verified,
                                           @RequestParam(required = false) Boolean active,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String sort) {
        return service.search(q, role, verified, active, page, size, sort);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Integer id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Integer id, @Valid @RequestBody UserUpdateRequest req) {
        return service.update(id, req, CurrentUser.get().getId());
    }

    @PatchMapping("/{id}/deactivate")
    public UserResponse deactivate(@PathVariable Integer id) {
        return service.setActive(id, false, CurrentUser.get().getId());
    }

    @PatchMapping("/{id}/activate")
    public UserResponse activate(@PathVariable Integer id) {
        return service.setActive(id, true, CurrentUser.get().getId());
    }
}
