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
@RequestMapping("/api/neighborhoods")
public class NeighborhoodController {

    private final CatalogService catalog;

    public NeighborhoodController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public PageResponse<NeighborhoodResponse> list(@RequestParam(required = false) String q,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) String sort) {
        return catalog.searchNeighborhoods(q, page, size, sort);
    }

    @GetMapping("/options")
    public List<NeighborhoodResponse> options() {
        return catalog.allNeighborhoods();
    }

    @GetMapping("/{id}")
    public NeighborhoodResponse get(@PathVariable Integer id) {
        return catalog.getNeighborhood(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALISTA')")
    public ResponseEntity<NeighborhoodResponse> create(@Valid @RequestBody NeighborhoodRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalog.createNeighborhood(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALISTA')")
    public NeighborhoodResponse update(@PathVariable Integer id, @Valid @RequestBody NeighborhoodRequest req) {
        return catalog.updateNeighborhood(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALISTA')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        catalog.deleteNeighborhood(id);
        return ResponseEntity.noContent().build();
    }
}
