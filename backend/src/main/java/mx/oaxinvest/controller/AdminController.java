package mx.oaxinvest.controller;

import mx.oaxinvest.dto.AdminDtos.*;
import mx.oaxinvest.dto.PageResponse;
import mx.oaxinvest.model.EmailType;
import mx.oaxinvest.model.SendStatus;
import mx.oaxinvest.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return service.dashboard();
    }

    @GetMapping("/email-logs")
    public PageResponse<EmailLogResponse> emailLogs(@RequestParam(required = false) String q,
                                                    @RequestParam(required = false) EmailType type,
                                                    @RequestParam(required = false) SendStatus status,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(required = false) String sort) {
        return service.searchEmailLogs(q, type, status, page, size, sort);
    }
}
