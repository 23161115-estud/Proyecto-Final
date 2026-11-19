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
@Table(name = "simulations")
@Getter
@Setter
@NoArgsConstructor
public class Simulation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "neighborhood_id")
    private Neighborhood neighborhood;

    @Column(name = "land_width", nullable = false)
    private BigDecimal landWidth;

    @Column(name = "land_length", nullable = false)
    private BigDecimal landLength;

    @Column(name = "total_area", nullable = false)
    private BigDecimal totalArea;

    @Column(name = "num_apartments", nullable = false)
    private Integer numApartments;

    @Column(name = "construction_cost_per_m2", nullable = false)
    private BigDecimal constructionCostPerM2;

    @Column(name = "monthly_rent_per_apartment", nullable = false)
    private BigDecimal monthlyRentPerApartment;

    @Column(name = "fixed_monthly_expenses", nullable = false)
    private BigDecimal fixedMonthlyExpenses;

    @Column(name = "calculated_total_cost", nullable = false)
    private BigDecimal calculatedTotalCost;

    @Column(name = "calculated_annual_income", nullable = false)
    private BigDecimal calculatedAnnualIncome;

    @Column(name = "calculated_roi", nullable = false)
    private BigDecimal calculatedRoi;

    @Column(name = "calculated_payback_months", nullable = false)
    private BigDecimal calculatedPaybackMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_semaphore", nullable = false)
    private Semaphore statusSemaphore;

    @Column(name = "ai_analysis_text", columnDefinition = "TEXT")
    private String aiAnalysisText;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "simulation_amenities",
            joinColumns = @JoinColumn(name = "simulation_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id"))
    private Set<Amenity> amenities = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
