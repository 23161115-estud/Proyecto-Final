package mx.oaxinvest.controller;

import jakarta.validation.Valid;
import mx.oaxinvest.dto.CatalogDtos.*;
import mx.oaxinvest.dto.PageResponse;
import mx.oaxinvest.service.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
public class AmenityController {

    private final CatalogService catalog;

    public AmenityController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public PageResponse<AmenityResponse> list(@RequestParam(required = false) String q,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String sort) {
        return catalog.searchAmenities(q, page, size, sort);
    }

    @GetMapping("/options")
    public List<AmenityResponse> options() {
        return catalog.allAmenities();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALISTA')")
    public ResponseEntity<AmenityResponse> create(@Valid @RequestBody AmenityRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalog.createAmenity(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALISTA')")
    public AmenityResponse update(@PathVariable Integer id, @Valid @RequestBody AmenityRequest req) {
        return catalog.updateAmenity(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALISTA')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        catalog.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
}
