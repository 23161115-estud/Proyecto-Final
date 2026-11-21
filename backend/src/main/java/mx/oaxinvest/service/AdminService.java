package mx.oaxinvest.service;

import jakarta.persistence.criteria.Predicate;
import mx.oaxinvest.dto.AdminDtos.*;
import mx.oaxinvest.dto.PageResponse;
import mx.oaxinvest.model.EmailLog;
import mx.oaxinvest.model.EmailType;
import mx.oaxinvest.model.SendStatus;
import mx.oaxinvest.model.Semaphore;
import mx.oaxinvest.repository.EmailLogRepository;
import mx.oaxinvest.repository.SimulationRepository;
import mx.oaxinvest.repository.UserRepository;
import mx.oaxinvest.util.PageUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private static final Map<String, String> SORT = Map.of("sentAt", "sentAt", "recipient", "recipientEmail",
            "status", "status");

    private final UserRepository users;
    private final SimulationRepository sims;
    private final EmailLogRepository emails;

    public AdminService(UserRepository users, SimulationRepository sims, EmailLogRepository emails) {
        this.users = users;
        this.sims = sims;
        this.emails = emails;
    }

    public DashboardResponse dashboard() {
        Map<String, Long> bySemaphore = new LinkedHashMap<>();
        for (Semaphore s : Semaphore.values()) {
            bySemaphore.put(s.name(), 0L);
        }
        for (Object[] row : sims.countBySemaphore()) {
            bySemaphore.put(((Semaphore) row[0]).name(), (Long) row[1]);
        }
        var from = LocalDate.now().withDayOfMonth(1).minusMonths(5).atStartOfDay();
        List<MonthCount> months = sims.countByMonth(from).stream()
                .map(m -> new MonthCount(m.getYm(), m.getTotal())).toList();
        List<EmailLogResponse> recent = emails.findTop5ByOrderBySentAtDescIdDesc().stream()
                .map(EmailLogResponse::from).toList();
        return new DashboardResponse(users.count(), sims.count(), emails.countByStatus(SendStatus.SENT),
                emails.countByStatus(SendStatus.FAILED), emails.countByStatus(SendStatus.PENDING),
                bySemaphore, months, recent);
    }

    public PageResponse<EmailLogResponse> searchEmailLogs(String q, EmailType type, SendStatus status,
                                                          int page, int size, String sort) {
        Specification<EmailLog> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                p.add(cb.like(cb.lower(root.get("recipientEmail")), PageUtil.like(q), '\\'));
            }
            if (type != null) {
                p.add(cb.equal(root.get("emailType"), type));
            }
            if (status != null) {
                p.add(cb.equal(root.get("status"), status));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        Pageable pageable = PageUtil.of(page, size, sort, SORT, "sentAt");
        return PageResponse.of(emails.findAll(spec, pageable).map(EmailLogResponse::from));
    }
}
