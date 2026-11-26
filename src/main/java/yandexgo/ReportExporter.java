package yandexgo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ReportExporter { void export(List<RideRecord> records, Path path) throws IOException; }
