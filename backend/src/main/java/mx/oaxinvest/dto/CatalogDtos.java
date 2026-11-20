package mx.oaxinvest.dto;

import jakarta.validation.constraints.*;
import mx.oaxinvest.model.AiTrainingData;
import mx.oaxinvest.model.Amenity;
import mx.oaxinvest.model.Neighborhood;
import java.math.BigDecimal;

public final class CatalogDtos {
    private CatalogDtos() {
    }

    public record NeighborhoodRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre no puede exceder 100 caracteres") String name,
            @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres") String city,
            @NotNull(message = "El costo del terreno es obligatorio")
            @DecimalMin(value = "1.00", message = "El costo del terreno debe ser mayor a 0")
            @Digits(integer = 8, fraction = 2, message = "Formato de costo inválido") BigDecimal averageLandCostM2,
            @NotNull(message = "El costo de construcción es obligatorio")
            @DecimalMin(value = "1.00", message = "El costo de construcción debe ser mayor a 0")
            @Digits(integer = 8, fraction = 2, message = "Formato de costo inválido") BigDecimal constructionCostM2,
            @NotNull(message = "El factor de zona es obligatorio")
            @DecimalMin(value = "0.10", message = "El factor de zona mínimo es 0.10")
            @DecimalMax(value = "9.99", message = "El factor de zona máximo es 9.99") BigDecimal zoneFactor) {
    }

    public record NeighborhoodResponse(Integer id, String name, String city, BigDecimal averageLandCostM2,
                                       BigDecimal constructionCostM2, BigDecimal zoneFactor) {
        public static NeighborhoodResponse from(Neighborhood n) {
            return new NeighborhoodResponse(n.getId(), n.getName(), n.getCity(), n.getAverageLandCostM2(),
                    n.getConstructionCostM2(), n.getZoneFactor());
        }
    }

    public record AmenityRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 80, message = "El nombre no puede exceder 80 caracteres") String name,
            @Size(max = 255, message = "La descripción no puede exceder 255 caracteres") String description,
            @NotNull(message = "El porcentaje de costo extra es obligatorio")
            @DecimalMin(value = "0.00", message = "El porcentaje no puede ser negativo")
            @DecimalMax(value = "99.99", message = "El porcentaje máximo es 99.99")
            @Digits(integer = 2, fraction = 2, message = "Formato de porcentaje inválido") BigDecimal extraCostPercentage) {
    }

    public record AmenityResponse(Integer id, String name, String description, BigDecimal extraCostPercentage) {
        public static AmenityResponse from(Amenity a) {
            return new AmenityResponse(a.getId(), a.getName(), a.getDescription(), a.getExtraCostPercentage());
        }
    }

    public record AiDataRequest(
            @NotBlank(message = "La colonia es obligatoria")
            @Size(max = 100, message = "La colonia no puede exceder 100 caracteres") String neighborhoodName,
            @NotNull(message = "Los m² del departamento son obligatorios")
            @DecimalMin(value = "5.00", message = "El departamento debe medir al menos 5 m²")
            @DecimalMax(value = "999.99", message = "Los m² máximos son 999.99") BigDecimal apartmentM2,
            @NotNull(message = "La distancia a la universidad es obligatoria")
            @Min(value = 0, message = "La distancia no puede ser negativa")
            @Max(value = 100000, message = "La distancia máxima es 100000 m") Integer distanceToUniversityM,
            @NotNull(message = "Indica si hay transporte cercano") Boolean hasNearbyTransport,
            @NotNull(message = "La renta real es obligatoria")
            @DecimalMin(value = "1.00", message = "La renta debe ser mayor a 0")
            @Digits(integer = 8, fraction = 2, message = "Formato de renta inválido") BigDecimal realMonthlyRent) {
    }

    public record AiDataResponse(Integer id, String neighborhoodName, BigDecimal apartmentM2,
                                 Integer distanceToUniversityM, boolean hasNearbyTransport, BigDecimal realMonthlyRent) {
        public static AiDataResponse from(AiTrainingData a) {
            return new AiDataResponse(a.getId(), a.getNeighborhoodName(), a.getApartmentM2(),
                    a.getDistanceToUniversityM(), a.isHasNearbyTransport(), a.getRealMonthlyRent());
        }
    }
}
