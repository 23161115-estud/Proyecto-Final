package mx.oaxinvest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ai_training_data")
@Getter
@Setter
@NoArgsConstructor
public class AiTrainingData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "neighborhood_name", nullable = false, length = 100)
    private String neighborhoodName;

    @Column(name = "apartment_m2", nullable = false)
    private BigDecimal apartmentM2;

    @Column(name = "distance_to_university_m", nullable = false)
    private Integer distanceToUniversityM;

    @Column(name = "has_nearby_transport", nullable = false)
    private boolean hasNearbyTransport;

    @Column(name = "real_monthly_rent", nullable = false)
    private BigDecimal realMonthlyRent;

    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;
}
