package mx.oaxinvest.controller;

import jakarta.validation.Valid;
import mx.oaxinvest.dto.MessageResponse;
import mx.oaxinvest.dto.PageResponse;
import mx.oaxinvest.dto.SimulationDtos.*;
import mx.oaxinvest.model.Semaphore;
import mx.oaxinvest.security.CurrentUser;
import mx.oaxinvest.service.SimulationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulations")
@PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
public class SimulationController {

    private final SimulationService service;

    public SimulationController(SimulationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SimulationDetail> create(@Valid @RequestBody SimulationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req, CurrentUser.get()));
    }

    @GetMapping
    public PageResponse<SimulationSummary> list(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) Semaphore semaphore,
                                                @RequestParam(required = false) Integer userId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String sort) {
        return service.search(CurrentUser.get(), q, semaphore, userId, page, size, sort);
    }

    @GetMapping("/{id}")
    public SimulationDetail get(@PathVariable Integer id) {
        return service.getDetail(id, CurrentUser.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id, CurrentUser.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Integer id) {
        byte[] bytes = service.downloadPdf(id, CurrentUser.get());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"oaxinvest-simulacion-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF).body(bytes);
    }

    @PostMapping("/{id}/send-email")
    public MessageResponse sendEmail(@PathVariable Integer id) {
        return service.sendEmail(id, CurrentUser.get());
    }

    @PostMapping("/{id}/send-sms")
    public MessageResponse sendSms(@PathVariable Integer id, @Valid @RequestBody(required = false) SendMessageRequest req) {
        return service.sendSms(id, CurrentUser.get(), req == null ? null : req.phone());
    }

    @PostMapping("/{id}/send-whatsapp")
    public MessageResponse sendWhatsapp(@PathVariable Integer id, @Valid @RequestBody(required = false) SendMessageRequest req) {
        return service.sendWhatsapp(id, CurrentUser.get(), req == null ? null : req.phone());
    }
}
