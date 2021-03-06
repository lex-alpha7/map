package ru.akhitev.rp.star_system.repo;


import org.springframework.stereotype.Repository;
import ru.akhitev.rp.star_system.entity.StarSystem;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class StarSystemRepositoryCustomImpl implements StarSystemRepositoryCustom {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<StarSystem> findNearCoordinates(double x, double y) {
        final String queryTemplate = "SELECT ss.* FROM Star_System as ss WHERE ss.coordinateX >= ? AND ss.coordinateX <= ? AND ss.coordinateY >= ? AND ss.coordinateY <= ?";
        Query query = entityManager.createNativeQuery(queryTemplate, StarSystem.class);
        query.setParameter(1, x - 15);
        query.setParameter(2, x + 15);
        query.setParameter(3, y - 15);
        query.setParameter(4, y + 15);
        return query.getResultList();
    }
}
