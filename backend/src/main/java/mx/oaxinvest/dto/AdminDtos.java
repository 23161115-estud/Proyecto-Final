package mx.oaxinvest.dto;

import mx.oaxinvest.model.EmailLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class AdminDtos {
    private AdminDtos() {
    }

    public record EmailLogResponse(Integer id, Integer userId, String emailType, String recipientEmail,
                                   String status, String errorMessage, LocalDateTime sentAt) {
        public static EmailLogResponse from(EmailLog e) {
            return new EmailLogResponse(e.getId(), e.getUserId(), e.getEmailType().name(), e.getRecipientEmail(),
                    e.getStatus().name(), e.getErrorMessage(), e.getSentAt());
        }
    }

    public record MonthCount(String month, long total) {
    }

    public record DashboardResponse(long users, long simulations, long emailsSent, long emailsFailed,
                                    long emailsPending, Map<String, Long> bySemaphore,
                                    List<MonthCount> simulationsByMonth, List<EmailLogResponse> recentEmails) {
    }
}
