package yandexgo;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public final class Analytics {
    public BigDecimal completedRevenue(List<RideRecord> records) {
        return records.stream().filter(r -> r.status() == RideStatus.COMPLETED)
                .map(RideRecord::price).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public Map<Tariff, Long> countByTariff(List<RideRecord> records) {
        return records.stream().collect(Collectors.groupingBy(RideRecord::tariff,
                () -> new EnumMap<>(Tariff.class), Collectors.counting()));
    }
    public Set<String> servedPassengers(List<RideRecord> records) {
        return records.stream().filter(r -> r.status() == RideStatus.COMPLETED)
                .map(RideRecord::passenger).collect(Collectors.toCollection(TreeSet::new));
    }
}
