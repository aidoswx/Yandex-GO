package yandexgo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class PersistenceTest {
    @TempDir Path temp;
    String url() { return "jdbc:sqlite:" + temp.resolve("test.db"); }
    RideRecord record(String id, RideStatus status) {
        return new RideRecord(id, "Алия", "Данияр", "A", "B", Tariff.COMFORT, status, new BigDecimal("7.5"), new BigDecimal("1925.00"), null);
    }
    @Test void dataSurvivesDaoRecreation() throws Exception {
        RideDao dao = new SqliteRideDao(url()); RideRecord r = record("1", RideStatus.COMPLETED);
        dao.save(r); assertEquals(r, new SqliteRideDao(url()).findAll().get(0));
    }
    @Test void updatesInsteadOfDuplicatingAndAppendsEvents() throws Exception {
        RideDao dao = new SqliteRideDao(url()); dao.save(record("1", RideStatus.ASSIGNED));
        dao.save(record("1", RideStatus.COMPLETED));
        assertEquals(1, dao.findAll().size()); assertEquals(RideStatus.COMPLETED, dao.findAll().get(0).status());
        try (Connection c = DriverManager.getConnection(url()); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT count(*) FROM ride_events")) {
            assertTrue(r.next()); assertEquals(2, r.getInt(1));
        }
    }
    @Test void failureOfSecondStatementRollsBackFirst() throws Exception {
        RideDao dao = new SqliteRideDao(url()); dao.save(record("1", RideStatus.ASSIGNED));
        try (Connection c = DriverManager.getConnection(url()); Statement s = c.createStatement()) {
            s.execute("CREATE TRIGGER fail_event BEFORE INSERT ON ride_events BEGIN SELECT RAISE(ABORT, 'test failure'); END");
        }
        assertThrows(SQLException.class, () -> dao.save(record("1", RideStatus.COMPLETED)));
        assertEquals(RideStatus.ASSIGNED, dao.findAll().get(0).status());
        assertThrows(SQLException.class, () -> dao.save(record("2", RideStatus.CREATED)));
        assertEquals(1, dao.findAll().size());
    }
    @Test void sqlLikeTextIsStoredAsData() throws Exception {
        RideDao dao = new SqliteRideDao(url()); String id = "'; DROP TABLE rides; --";
        dao.save(record(id, RideStatus.CREATED)); assertEquals(id, dao.findAll().get(0).id());
    }
    @Test void unassignedRideHasNullPriceAndRating() throws Exception {
        RideDao dao = new SqliteRideDao(url());
        dao.save(RideRecord.from(new Ride(new Passenger("A"), Tariff.ECONOMY, "A", "B", BigDecimal.ONE)));
        RideRecord r = dao.findAll().get(0); assertNull(r.price()); assertNull(r.rating()); assertNull(r.driver());
    }
}
