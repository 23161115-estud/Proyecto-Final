package mx.oaxinvest.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import mx.oaxinvest.dto.SimulationDtos.SimulationDetail;
import mx.oaxinvest.exception.ExternalServiceException;
import mx.oaxinvest.model.PdfReport;
import mx.oaxinvest.repository.PdfReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PdfService {

    private static final Color GREEN = new Color(0x1D, 0x5E, 0x4B);
    private static final Color INK = new Color(0x1E, 0x25, 0x23);

    private final PdfReportRepository reports;
    private final String pdfDir;

    public PdfService(PdfReportRepository reports, @Value("${app.pdf-dir}") String pdfDir) {
        this.reports = reports;
        this.pdfDir = pdfDir;
    }

    public byte[] build(SimulationDetail d) {
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("OaxInvest", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, GREEN)));
            doc.add(new Paragraph("Reporte de simulación #" + d.id() + " · " + d.neighborhoodName(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, INK)));
            doc.add(new Paragraph("Generado para " + d.userName() + " el " + d.createdAt().toLocalDate(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY)));
            doc.add(Chunk.NEWLINE);

            Color sem = switch (d.semaphore()) {
                case VERDE -> new Color(0x2E, 0x9E, 0x5B);
                case AMARILLO -> new Color(0xE8, 0xB1, 0x00);
                case ROJO -> new Color(0xD9, 0x3A, 0x3A);
            };
            doc.add(new Paragraph("Semáforo de inversión: " + d.semaphore(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, sem)));
            doc.add(Chunk.NEWLINE);

            PdfPTable t = new PdfPTable(2);
            t.setWidthPercentage(100);
            row(t, "Terreno", d.landWidth() + " m x " + d.landLength() + " m (" + d.totalArea() + " m²)");
            row(t, "Departamentos", String.valueOf(d.numApartments()));
            row(t, "Costo de construcción por m²", money.format(d.constructionCostPerM2()));
            row(t, "Renta mensual por departamento", money.format(d.monthlyRentPerApartment()));
            row(t, "Gastos fijos mensuales", money.format(d.fixedMonthlyExpenses()));
            row(t, "Amenidades", d.amenities().isEmpty() ? "Ninguna"
                    : d.amenities().stream().map(a -> a.name()).collect(Collectors.joining(", ")));
            row(t, "Costo total estimado", money.format(d.totalCost()));
            row(t, "Ingreso neto anual", money.format(d.annualIncome()));
            row(t, "ROI anual", d.roi() + " %");
            row(t, "Recuperación de la inversión", d.paybackMonths() + " meses");
            doc.add(t);

            if (d.aiAnalysis() != null && !d.aiAnalysis().isBlank()) {
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph("Análisis", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, GREEN)));
                doc.add(new Paragraph(d.aiAnalysis(), FontFactory.getFont(FontFactory.HELVETICA, 11, INK)));
            }
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Estimación informativa. No sustituye un estudio financiero profesional.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY)));
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
        return out.toByteArray();
    }

    public void register(Integer simulationId, String sentToEmail, byte[] pdf) {
        try {
            Path dir = Path.of(pdfDir);
            Files.createDirectories(dir);
            Path file = dir.resolve("report_" + simulationId + "_" + System.currentTimeMillis() + ".pdf");
            Files.write(file, pdf);
            PdfReport r = new PdfReport();
            r.setSimulationId(simulationId);
            r.setFilePath(file.toString());
            r.setSentToEmail(sentToEmail);
            reports.save(r);
        } catch (IOException e) {
            throw new ExternalServiceException("No se pudo guardar el PDF en el servidor");
        }
    }

    private void row(PdfPTable t, String label, String value) {
        PdfPCell a = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, INK)));
        PdfPCell b = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA, 10, INK)));
        a.setPadding(7);
        b.setPadding(7);
        a.setBackgroundColor(new Color(0xDC, 0xEB, 0xE5));
        t.addCell(a);
        t.addCell(b);
    }
}
