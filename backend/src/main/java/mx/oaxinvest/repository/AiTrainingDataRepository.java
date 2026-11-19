package mx.oaxinvest.repository;

import mx.oaxinvest.model.AiTrainingData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiTrainingDataRepository extends JpaRepository<AiTrainingData, Integer> {

    @Query("select a from AiTrainingData a where lower(a.neighborhoodName) like lower(concat('%', :q, '%')) " +
            "and (:transport is null or a.hasNearbyTransport = :transport)")
    Page<AiTrainingData> search(@Param("q") String q, @Param("transport") Boolean transport, Pageable pageable);

    @Query("select avg(a.realMonthlyRent) from AiTrainingData a where lower(a.neighborhoodName) = lower(:name)")
    Double averageRentByNeighborhood(@Param("name") String name);
}
