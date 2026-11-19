package mx.oaxinvest.repository;

import mx.oaxinvest.model.PdfReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PdfReportRepository extends JpaRepository<PdfReport, Integer> {
}
