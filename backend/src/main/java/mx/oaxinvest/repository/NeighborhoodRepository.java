package mx.oaxinvest.repository;

import mx.oaxinvest.model.Neighborhood;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Integer> {
    Page<Neighborhood> findByNameContainingIgnoreCase(String q, Pageable pageable);
}
