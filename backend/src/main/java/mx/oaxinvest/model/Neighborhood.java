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
@Table(name = "neighborhoods")
@Getter
@Setter
@NoArgsConstructor
public class Neighborhood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String city = "Oaxaca de Juárez";

    @Column(name = "average_land_cost_m2", nullable = false)
    private BigDecimal averageLandCostM2;

    @Column(name = "construction_cost_m2", nullable = false)
    private BigDecimal constructionCostM2;

    @Column(name = "zone_factor", nullable = false)
    private BigDecimal zoneFactor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
