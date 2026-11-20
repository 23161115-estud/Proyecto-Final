package mx.oaxinvest.dto;

import jakarta.validation.constraints.*;
import mx.oaxinvest.util.ValidationPatterns;
import mx.oaxinvest.model.Semaphore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class SimulationDtos {
    private SimulationDtos() {
    }

    public record SimulationRequest(
            @NotNull(message = "Elige una colonia") @Positive(message = "Colonia inválida") Integer neighborhoodId,
            @NotNull(message = "El ancho es obligatorio")
            @DecimalMin(value = "1.00", message = "El ancho mínimo es 1 m")
            @DecimalMax(value = "500.00", message = "El ancho máximo es 500 m") BigDecimal landWidth,
            @NotNull(message = "El largo es obligatorio")
            @DecimalMin(value = "1.00", message = "El largo mínimo es 1 m")
            @DecimalMax(value = "500.00", message = "El largo máximo es 500 m") BigDecimal landLength,
            @NotNull(message = "El número de departamentos es obligatorio")
            @Min(value = 1, message = "Mínimo 1 departamento")
            @Max(value = 200, message = "Máximo 200 departamentos") Integer numApartments,
            @NotNull(message = "El costo de construcción es obligatorio")
            @DecimalMin(value = "100.00", message = "El costo mínimo es $100 por m²")
            @DecimalMax(value = "100000.00", message = "El costo máximo es $100,000 por m²") BigDecimal constructionCostPerM2,
            @NotNull(message = "La renta mensual es obligatoria")
            @DecimalMin(value = "1.00", message = "La renta debe ser mayor a 0")
            @DecimalMax(value = "1000000.00", message = "La renta máxima es $1,000,000") BigDecimal monthlyRentPerApartment,
            @NotNull(message = "Los gastos fijos son obligatorios")
            @DecimalMin(value = "0.00", message = "Los gastos no pueden ser negativos")
            @DecimalMax(value = "10000000.00", message = "Los gastos máximos son $10,000,000") BigDecimal fixedMonthlyExpenses,
            Set<@NotNull(message = "Amenidad inválida") Integer> amenityIds) {
    }

    public record SimulationSummary(Integer id, Integer userId, String userName, String userEmail,
                                    String neighborhoodName, BigDecimal totalArea, Integer numApartments,
                                    BigDecimal roi, BigDecimal paybackMonths, Semaphore semaphore,
                                    LocalDateTime createdAt) {
    }

    public record SimulationDetail(Integer id, Integer userId, String userName, Integer neighborhoodId,
                                   String neighborhoodName, BigDecimal landWidth, BigDecimal landLength,
                                   BigDecimal totalArea, Integer numApartments, BigDecimal constructionCostPerM2,
                                   BigDecimal monthlyRentPerApartment, BigDecimal fixedMonthlyExpenses,
                                   BigDecimal totalCost, BigDecimal annualIncome, BigDecimal roi,
                                   BigDecimal paybackMonths, Semaphore semaphore, String aiAnalysis,
                                   List<CatalogDtos.AmenityResponse> amenities, LocalDateTime createdAt) {
    }

    public record SendMessageRequest(
            @Pattern(regexp = ValidationPatterns.PHONE, message = ValidationPatterns.PHONE_MSG) String phone) {
    }
}
