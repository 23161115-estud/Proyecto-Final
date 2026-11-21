package mx.oaxinvest.controller;

import jakarta.validation.Valid;
import mx.oaxinvest.dto.CatalogDtos.*;
import mx.oaxinvest.dto.PageResponse;
import mx.oaxinvest.service.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-data")
@PreAuthorize("hasAnyRole('ADMIN','ANALISTA')")
public class AiDataController {

    private final CatalogService catalog;

    public AiDataController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public PageResponse<AiDataResponse> list(@RequestParam(required = false) String q,
                                             @RequestParam(required = false) Boolean transport,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String sort) {
        return catalog.searchAiData(q, transport, page, size, sort);
    }

    @PostMapping
    public ResponseEntity<AiDataResponse> create(@Valid @RequestBody AiDataRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalog.createAiData(req));
    }

    @PutMapping("/{id}")
    public AiDataResponse update(@PathVariable Integer id, @Valid @RequestBody AiDataRequest req) {
        return catalog.updateAiData(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        catalog.deleteAiData(id);
        return ResponseEntity.noContent().build();
    }
}
