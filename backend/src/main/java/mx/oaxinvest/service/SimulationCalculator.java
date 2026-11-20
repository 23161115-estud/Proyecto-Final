package mx.oaxinvest.service;

import mx.oaxinvest.model.Amenity;
import mx.oaxinvest.model.Semaphore;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

@Component
public class SimulationCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal MAX_ROI = new BigDecimal("999.99");
    private static final BigDecimal MAX_PAYBACK = new BigDecimal("9999.99");

    public record Result(BigDecimal totalArea, BigDecimal totalCost, BigDecimal annualIncome, BigDecimal roi,
                         BigDecimal paybackMonths, Semaphore semaphore) {
    }

    public Result calculate(BigDecimal width, BigDecimal length, int apartments, BigDecimal costPerM2,
                            BigDecimal rentPerApartment, BigDecimal fixedExpenses, Collection<Amenity> amenities) {
        BigDecimal area = width.multiply(length).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pct = amenities.stream().map(Amenity::getExtraCostPercentage).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal factor = BigDecimal.ONE.add(pct.divide(HUNDRED, 4, RoundingMode.HALF_UP));
        BigDecimal totalCost = area.multiply(costPerM2).multiply(factor).setScale(2, RoundingMode.HALF_UP);

        BigDecimal netMonthly = rentPerApartment.multiply(BigDecimal.valueOf(apartments)).subtract(fixedExpenses);
        BigDecimal annual = netMonthly.multiply(BigDecimal.valueOf(12)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal roi = totalCost.signum() == 0 ? BigDecimal.ZERO
                : annual.multiply(HUNDRED).divide(totalCost, 2, RoundingMode.HALF_UP);
        roi = roi.min(MAX_ROI).max(MAX_ROI.negate());

        BigDecimal payback = netMonthly.signum() > 0
                ? totalCost.divide(netMonthly, 2, RoundingMode.HALF_UP).min(MAX_PAYBACK)
                : MAX_PAYBACK;

        Semaphore s = roi.compareTo(BigDecimal.valueOf(15)) >= 0 ? Semaphore.VERDE
                : roi.compareTo(BigDecimal.valueOf(8)) >= 0 ? Semaphore.AMARILLO : Semaphore.ROJO;
        return new Result(area, totalCost, annual, roi, payback, s);
    }
}
