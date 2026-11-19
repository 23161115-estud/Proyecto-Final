package mx.oaxinvest.repository;

import mx.oaxinvest.model.Simulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SimulationRepository extends JpaRepository<Simulation, Integer>, JpaSpecificationExecutor<Simulation> {

    @Override
    @EntityGraph(attributePaths = {"neighborhood", "user"})
    Page<Simulation> findAll(Specification<Simulation> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"neighborhood", "user", "amenities"})
    Optional<Simulation> findDetailedById(Integer id);

    @Query("select s.statusSemaphore, count(s) from Simulation s group by s.statusSemaphore")
    List<Object[]> countBySemaphore();

    interface MonthCount {
        String getYm();
        Long getTotal();
    }

    @Query(value = "SELECT DATE_FORMAT(created_at,'%Y-%m') AS ym, COUNT(*) AS total FROM simulations " +
            "WHERE created_at >= :from GROUP BY ym ORDER BY ym", nativeQuery = true)
    List<MonthCount> countByMonth(@Param("from") LocalDateTime from);
}
