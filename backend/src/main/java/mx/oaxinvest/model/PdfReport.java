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
@Table(name = "pdf_reports")
@Getter
@Setter
@NoArgsConstructor
public class PdfReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "simulation_id", nullable = false)
    private Integer simulationId;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "sent_to_email", nullable = false, length = 150)
    private String sentToEmail;

    @CreationTimestamp
    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;
}
