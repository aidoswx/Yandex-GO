package yandexgo;

import java.sql.SQLException;
import java.util.List;

public interface RideDao {
    void save(RideRecord ride) throws SQLException;
    List<RideRecord> findAll() throws SQLException;
}
