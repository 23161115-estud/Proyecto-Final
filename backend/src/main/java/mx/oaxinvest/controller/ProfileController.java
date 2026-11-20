package mx.oaxinvest.controller;

import jakarta.validation.Valid;
import mx.oaxinvest.dto.MessageResponse;
import mx.oaxinvest.dto.UserDtos.*;
import mx.oaxinvest.security.CurrentUser;
import mx.oaxinvest.service.ProfileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profile;

    public ProfileController(ProfileService profile) {
        this.profile = profile;
    }

    @GetMapping
    public UserResponse me() {
        return profile.get(CurrentUser.get().getId());
    }

    @PutMapping
    public UserResponse update(@Valid @RequestBody ProfileUpdateRequest req) {
        return profile.update(CurrentUser.get().getId(), req);
    }

    @PutMapping("/password")
    public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        return profile.changePassword(CurrentUser.get().getId(), req);
    }

    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    public UserResponse avatar(@RequestParam("file") MultipartFile file) {
        return profile.uploadAvatar(CurrentUser.get().getId(), file);
    }

    @DeleteMapping
    public MessageResponse delete() {
        return profile.deleteAccount(CurrentUser.get().getId());
    }
}
