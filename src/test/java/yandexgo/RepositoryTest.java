package yandexgo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {
    @Test void savesAndFindsTypedEntities() {
        Repository<Passenger> repo = new Repository<>(); Passenger p = new Passenger("P");
        repo.save(p); assertSame(p, repo.findById(p.getId()).orElseThrow());
    }
    @Test void sameIdDoesNotDuplicate() {
        Repository<Passenger> repo = new Repository<>(); Passenger p = new Passenger("P");
        repo.save(p); repo.save(p); assertEquals(1, repo.findAll().size());
    }
    @Test void missingIsEmptyAndSnapshotIsIndependent() {
        Repository<Driver> repo = new Repository<>(); var snapshot = repo.findAll();
        repo.save(new Driver("D", null));
        assertTrue(repo.findById("missing").isEmpty()); assertTrue(snapshot.isEmpty());
    }
}
