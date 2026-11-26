package yandexgo;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public final class SqliteRideDao implements RideDao {
    private final String url;
    public SqliteRideDao(String url) throws SQLException {
        this.url = url;
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS rides (id TEXT PRIMARY KEY, passenger TEXT NOT NULL, driver TEXT, origin TEXT NOT NULL, destination TEXT NOT NULL, tariff TEXT NOT NULL, status TEXT NOT NULL, distance TEXT NOT NULL, price TEXT, rating INTEGER CHECK(rating BETWEEN 1 AND 5))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS ride_events (event_id INTEGER PRIMARY KEY AUTOINCREMENT, ride_id TEXT NOT NULL REFERENCES rides(id), status TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
    }
    private Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys=ON");
            s.execute("PRAGMA busy_timeout=5000");
        } catch (SQLException ex) { c.close(); throw ex; }
        return c;
    }
    /** Бір транзакция: сапар күйін сақтау + оқиға журналын жазу. */
    @Override public void save(RideRecord r) throws SQLException {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = c.prepareStatement("INSERT INTO rides VALUES (?,?,?,?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET passenger=excluded.passenger,driver=excluded.driver,origin=excluded.origin,destination=excluded.destination,tariff=excluded.tariff,status=excluded.status,distance=excluded.distance,price=excluded.price,rating=excluded.rating")) {
                    p.setString(1, r.id()); p.setString(2, r.passenger()); p.setString(3, r.driver());
                    p.setString(4, r.origin()); p.setString(5, r.destination());
                    p.setString(6, r.tariff().name()); p.setString(7, r.status().name());
                    p.setString(8, r.distance().toPlainString());
                    p.setString(9, r.price() == null ? null : r.price().toPlainString());
                    if (r.rating() == null) p.setNull(10, Types.INTEGER); else p.setInt(10, r.rating());
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement("INSERT INTO ride_events(ride_id,status) VALUES (?,?)")) {
                    p.setString(1, r.id()); p.setString(2, r.status().name()); p.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                try { c.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                throw ex;
            }
        }
    }
    @Override public List<RideRecord> findAll() throws SQLException {
        List<RideRecord> result = new ArrayList<>();
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM rides ORDER BY rowid")) {
            while (r.next()) {
                int rating = r.getInt("rating"); Integer nullableRating = r.wasNull() ? null : rating;
                String price = r.getString("price");
                result.add(new RideRecord(r.getString("id"), r.getString("passenger"), r.getString("driver"),
                        r.getString("origin"), r.getString("destination"), Tariff.valueOf(r.getString("tariff")),
                        RideStatus.valueOf(r.getString("status")), new BigDecimal(r.getString("distance")),
                        price == null ? null : new BigDecimal(price), nullableRating));
            }
        }
        return result;
    }
}
