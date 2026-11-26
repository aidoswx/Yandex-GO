package yandexgo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ReportingTest {
    @TempDir Path temp;
    RideRecord record(String id, String passenger, Tariff tariff, RideStatus status, String price) {
        return new RideRecord(id, passenger, null, "A", "B", tariff, status, BigDecimal.ONE,
                price == null ? null : new BigDecimal(price), null);
    }
    List<RideRecord> records() {
        return List.of(record("1", "A", Tariff.ECONOMY, RideStatus.COMPLETED, "600.00"),
                record("2", "A", Tariff.COMFORT, RideStatus.COMPLETED, "950.00"),
                record("3", "B", Tariff.COMFORT, RideStatus.ASSIGNED, "950.00"));
    }
    @Test void revenueIncludesOnlyCompletedRides() {
        assertEquals(new BigDecimal("1550.00"), new Analytics().completedRevenue(records()));
    }
    @Test void groupsByTariff() {
        assertEquals(Map.of(Tariff.ECONOMY, 1L, Tariff.COMFORT, 2L), new Analytics().countByTariff(records()));
    }
    @Test void setDeduplicatesServedPassengers() {
        assertEquals(Set.of("A"), new Analytics().servedPassengers(records()));
    }
    @Test void emptyAnalyticsIsValid() {
        assertEquals(BigDecimal.ZERO, new Analytics().completedRevenue(List.of()));
        assertTrue(new Analytics().countByTariff(List.of()).isEmpty());
    }
    @Test void csvEscapesQuotesCommasAndNewlinesInUtf8() throws Exception {
        Path output = temp.resolve("report.csv");
        ReportExporter exporter = new CsvReportExporter();
        exporter.export(List.of(record("1", "Алия, \"А\"\nБ", Tariff.ECONOMY, RideStatus.CREATED, null)), output);
        String text = Files.readString(output);
        assertTrue(text.startsWith("id,passenger,driver,"));
        assertTrue(text.contains("\"Алия, \"\"А\"\"\nБ\""));
        assertTrue(text.endsWith("\"\",\"\"\r\n"));
    }
    @Test void exportOverwritesPreviousReport() throws Exception {
        Path output = temp.resolve("report.csv");
        CsvReportExporter exporter = new CsvReportExporter(); exporter.export(records(), output);
        exporter.export(List.of(), output); assertEquals(1, Files.readAllLines(output).size());
    }
}
