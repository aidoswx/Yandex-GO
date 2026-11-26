package yandexgo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class PriceCalculator {
    public BigDecimal calculate(Tariff tariff, BigDecimal distanceKm) {
        Objects.requireNonNull(tariff);
        if (distanceKm == null || distanceKm.signum() <= 0) {
            throw new IllegalArgumentException("Расстояние должно быть больше нуля.");
        }
        return BigDecimal.valueOf(tariff.getBasePrice())
                .add(distanceKm.multiply(BigDecimal.valueOf(tariff.getPricePerKm())))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
