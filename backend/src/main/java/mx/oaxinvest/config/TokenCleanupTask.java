package mx.oaxinvest.config;

import mx.oaxinvest.repository.PasswordResetTokenRepository;
import mx.oaxinvest.repository.RevokedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
public class TokenCleanupTask {

    private final RevokedTokenRepository revoked;
    private final PasswordResetTokenRepository resets;

    public TokenCleanupTask(RevokedTokenRepository revoked, PasswordResetTokenRepository resets) {
        this.revoked = revoked;
        this.resets = resets;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        revoked.deleteExpired(now);
        resets.deleteExpired(now);
    }
}
