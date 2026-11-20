package mx.oaxinvest.repository;

import mx.oaxinvest.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Integer> {
}
