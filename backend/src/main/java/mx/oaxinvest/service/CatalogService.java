package mx.oaxinvest.service;

import mx.oaxinvest.dto.CatalogDtos.*;
import mx.oaxinvest.dto.PageResponse;
import mx.oaxinvest.exception.BusinessException;
import mx.oaxinvest.exception.ResourceNotFoundException;
import mx.oaxinvest.model.AiTrainingData;
import mx.oaxinvest.model.Amenity;
import mx.oaxinvest.model.Neighborhood;
import mx.oaxinvest.repository.AiTrainingDataRepository;
import mx.oaxinvest.repository.AmenityRepository;
import mx.oaxinvest.repository.NeighborhoodRepository;
import mx.oaxinvest.util.PageUtil;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CatalogService {

    private static final Map<String, String> N_SORT = Map.of("name", "name", "landCost", "averageLandCostM2",
            "constructionCost", "constructionCostM2", "zoneFactor", "zoneFactor");
    private static final Map<String, String> A_SORT = Map.of("name", "name", "extraCost", "extraCostPercentage");
    private static final Map<String, String> D_SORT = Map.of("neighborhood", "neighborhoodName", "rent", "realMonthlyRent",
            "distance", "distanceToUniversityM", "recordedAt", "recordedAt");

    private final NeighborhoodRepository neighborhoods;
    private final AmenityRepository amenities;
    private final AiTrainingDataRepository dataset;

    public CatalogService(NeighborhoodRepository neighborhoods, AmenityRepository amenities,
                          AiTrainingDataRepository dataset) {
        this.neighborhoods = neighborhoods;
        this.amenities = amenities;
        this.dataset = dataset;
    }

    @Transactional(readOnly = true)
    public PageResponse<NeighborhoodResponse> searchNeighborhoods(String q, int page, int size, String sort) {
        var pageable = PageUtil.of(page, size, sort == null ? "name,asc" : sort, N_SORT, "name");
        return PageResponse.of(neighborhoods.findByNameContainingIgnoreCase(q == null ? "" : q.trim(), pageable)
                .map(NeighborhoodResponse::from));
    }

    @Transactional(readOnly = true)
    public List<NeighborhoodResponse> allNeighborhoods() {
        return neighborhoods.findAll(Sort.by("name")).stream().map(NeighborhoodResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public NeighborhoodResponse getNeighborhood(Integer id) {
        return NeighborhoodResponse.from(findNeighborhood(id));
    }

    public NeighborhoodResponse createNeighborhood(NeighborhoodRequest r) {
        return NeighborhoodResponse.from(neighborhoods.save(fill(new Neighborhood(), r)));
    }

    public NeighborhoodResponse updateNeighborhood(Integer id, NeighborhoodRequest r) {
        return NeighborhoodResponse.from(fill(findNeighborhood(id), r));
    }

    public void deleteNeighborhood(Integer id) {
        neighborhoods.delete(findNeighborhood(id));
        neighborhoods.flush();
    }

    private Neighborhood findNeighborhood(Integer id) {
        return neighborhoods.findById(id).orElseThrow(() -> new ResourceNotFoundException("Colonia no encontrada"));
    }

    private Neighborhood fill(Neighborhood n, NeighborhoodRequest r) {
        n.setName(r.name().trim());
        n.setCity(r.city() == null || r.city().isBlank() ? "Oaxaca de Juárez" : r.city().trim());
        n.setAverageLandCostM2(r.averageLandCostM2());
        n.setConstructionCostM2(r.constructionCostM2());
        n.setZoneFactor(r.zoneFactor());
        return n;
    }

    @Transactional(readOnly = true)
    public PageResponse<AmenityResponse> searchAmenities(String q, int page, int size, String sort) {
        var pageable = PageUtil.of(page, size, sort == null ? "name,asc" : sort, A_SORT, "name");
        return PageResponse.of(amenities.findByNameContainingIgnoreCase(q == null ? "" : q.trim(), pageable)
                .map(AmenityResponse::from));
    }

    @Transactional(readOnly = true)
    public List<AmenityResponse> allAmenities() {
        return amenities.findAll(Sort.by("name")).stream().map(AmenityResponse::from).toList();
    }

    public AmenityResponse createAmenity(AmenityRequest r) {
        if (amenities.existsByNameIgnoreCase(r.name().trim())) {
            throw new BusinessException("Ya existe una amenidad con ese nombre");
        }
        Amenity a = new Amenity();
        return AmenityResponse.from(amenities.save(fill(a, r)));
    }

    public AmenityResponse updateAmenity(Integer id, AmenityRequest r) {
        Amenity a = amenities.findById(id).orElseThrow(() -> new ResourceNotFoundException("Amenidad no encontrada"));
        if (amenities.existsByNameIgnoreCaseAndIdNot(r.name().trim(), id)) {
            throw new BusinessException("Ya existe una amenidad con ese nombre");
        }
        return AmenityResponse.from(fill(a, r));
    }

    public void deleteAmenity(Integer id) {
        Amenity a = amenities.findById(id).orElseThrow(() -> new ResourceNotFoundException("Amenidad no encontrada"));
        amenities.delete(a);
    }

    private Amenity fill(Amenity a, AmenityRequest r) {
        a.setName(r.name().trim());
        a.setDescription(r.description());
        a.setExtraCostPercentage(r.extraCostPercentage());
        return a;
    }

    @Transactional(readOnly = true)
    public PageResponse<AiDataResponse> searchAiData(String q, Boolean transport, int page, int size, String sort) {
        var pageable = PageUtil.of(page, size, sort, D_SORT, "recordedAt");
        return PageResponse.of(dataset.search(q == null ? "" : q.trim(), transport, pageable).map(AiDataResponse::from));
    }

    public AiDataResponse createAiData(AiDataRequest r) {
        return AiDataResponse.from(dataset.save(fill(new AiTrainingData(), r)));
    }

    public AiDataResponse updateAiData(Integer id, AiDataRequest r) {
        AiTrainingData d = dataset.findById(id).orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
        return AiDataResponse.from(fill(d, r));
    }

    public void deleteAiData(Integer id) {
        AiTrainingData d = dataset.findById(id).orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
        dataset.delete(d);
    }

    private AiTrainingData fill(AiTrainingData d, AiDataRequest r) {
        d.setNeighborhoodName(r.neighborhoodName().trim());
        d.setApartmentM2(r.apartmentM2());
        d.setDistanceToUniversityM(r.distanceToUniversityM());
        d.setHasNearbyTransport(r.hasNearbyTransport());
        d.setRealMonthlyRent(r.realMonthlyRent());
        return d;
    }
}
