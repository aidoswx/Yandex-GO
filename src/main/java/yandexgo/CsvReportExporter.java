package yandexgo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public final class CsvReportExporter implements ReportExporter {
    @Override public void export(List<RideRecord> records, Path path) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write("id,passenger,driver,origin,destination,tariff,status,distance,price,rating\r\n");
            for (RideRecord r : records) {
                Object[] fields = {r.id(), r.passenger(), r.driver(), r.origin(), r.destination(), r.tariff(), r.status(), r.distance(), r.price(), r.rating()};
                for (int i = 0; i < fields.length; i++) {
                    if (i > 0) w.write(',');
                    String value = fields[i] == null ? "" : fields[i].toString();
                    w.write('"' + value.replace("\"", "\"\"") + '"');
                }
                w.write("\r\n");
            }
        }
    }
}
