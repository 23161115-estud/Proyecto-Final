package mx.oaxinvest.service;

import jakarta.persistence.criteria.Predicate;
import mx.oaxinvest.dto.CatalogDtos.AmenityResponse;
import mx.oaxinvest.dto.MessageResponse;
import mx.oaxinvest.dto.PageResponse;
import mx.oaxinvest.dto.SimulationDtos.*;
import mx.oaxinvest.exception.BusinessException;
import mx.oaxinvest.exception.ExternalServiceException;
import mx.oaxinvest.exception.ResourceNotFoundException;
import mx.oaxinvest.model.Amenity;
import mx.oaxinvest.model.Neighborhood;
import mx.oaxinvest.model.Semaphore;
import mx.oaxinvest.model.Simulation;
import mx.oaxinvest.model.User;
import mx.oaxinvest.repository.AmenityRepository;
import mx.oaxinvest.repository.NeighborhoodRepository;
import mx.oaxinvest.repository.SimulationRepository;
import mx.oaxinvest.repository.UserRepository;
import mx.oaxinvest.security.CurrentUser;
import mx.oaxinvest.util.PageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

@Service
public class SimulationService {

    private static final Map<String, String> SORT = Map.of("createdAt", "createdAt", "roi", "calculatedRoi",
            "payback", "calculatedPaybackMonths", "area", "totalArea");

    private final SimulationRepository sims;
    private final NeighborhoodRepository neighborhoods;
    private final AmenityRepository amenities;
    private final UserRepository users;
    private final SimulationCalculator calculator;
    private final AiAnalysisService ai;
    private final PdfService pdf;
    private final MailService mail;
    private final NotificationService notifications;
    private final TransactionTemplate tx;
    private final String frontendUrl;

    public SimulationService(SimulationRepository sims, NeighborhoodRepository neighborhoods, AmenityRepository amenities,
                             UserRepository users, SimulationCalculator calculator, AiAnalysisService ai,
                             PdfService pdf, MailService mail, NotificationService notifications,
                             TransactionTemplate tx, @Value("${app.frontend-url}") String frontendUrl) {
        this.sims = sims;
        this.neighborhoods = neighborhoods;
        this.amenities = amenities;
        this.users = users;
        this.calculator = calculator;
        this.ai = ai;
        this.pdf = pdf;
        this.mail = mail;
        this.notifications = notifications;
        this.tx = tx;
        this.frontendUrl = frontendUrl;
    }

    public SimulationDetail create(SimulationRequest r, User current) {
        Neighborhood n = neighborhoods.findById(r.neighborhoodId())
                .orElseThrow(() -> new ResourceNotFoundException("La colonia no existe"));
        Set<Amenity> selected = new HashSet<>();
        if (r.amenityIds() != null && !r.amenityIds().isEmpty()) {
            List<Amenity> found = amenities.findAllById(r.amenityIds());
            if (found.size() != r.amenityIds().size()) {
                throw new BusinessException("Alguna de las amenidades elegidas no existe");
            }
            selected.addAll(found);
        }
        SimulationCalculator.Result c = calculator.calculate(r.landWidth(), r.landLength(), r.numApartments(),
                r.constructionCostPerM2(), r.monthlyRentPerApartment(), r.fixedMonthlyExpenses(), selected);
        String analysis = ai.analyze(n.getName(), r.numApartments(), r.monthlyRentPerApartment(), c.roi(),
                c.paybackMonths(), c.semaphore());

        return tx.execute(status -> {
            Simulation s = new Simulation();
            s.setUser(users.getReferenceById(current.getId()));
            s.setNeighborhood(n);
            s.setLandWidth(r.landWidth());
            s.setLandLength(r.landLength());
            s.setTotalArea(c.totalArea());
            s.setNumApartments(r.numApartments());
            s.setConstructionCostPerM2(r.constructionCostPerM2());
            s.setMonthlyRentPerApartment(r.monthlyRentPerApartment());
            s.setFixedMonthlyExpenses(r.fixedMonthlyExpenses());
            s.setCalculatedTotalCost(c.totalCost());
            s.setCalculatedAnnualIncome(c.annualIncome());
            s.setCalculatedRoi(c.roi());
            s.setCalculatedPaybackMonths(c.paybackMonths());
            s.setStatusSemaphore(c.semaphore());
            s.setAiAnalysisText(analysis);
            s.setAmenities(selected);
            sims.save(s);
            return toDetail(s);
        });
    }

    @Transactional(readOnly = true)
    public PageResponse<SimulationSummary> search(User current, String q, Semaphore semaphore, Integer userId,
                                                  int page, int size, String sort) {
        boolean admin = CurrentUser.isAdmin(current);
        Specification<Simulation> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (!admin) {
                p.add(cb.equal(root.get("user").get("id"), current.getId()));
            } else if (userId != null) {
                p.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (q != null && !q.isBlank()) {
                String like = PageUtil.like(q);
                Predicate byNeighborhood = cb.like(cb.lower(root.get("neighborhood").get("name")), like, '\\');
                if (admin) {
                    p.add(cb.or(byNeighborhood, cb.like(cb.lower(root.get("user").get("name")), like, '\\'),
                            cb.like(cb.lower(root.get("user").get("email")), like, '\\')));
                } else {
                    p.add(byNeighborhood);
                }
            }
            if (semaphore != null) {
                p.add(cb.equal(root.get("statusSemaphore"), semaphore));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        Pageable pageable = PageUtil.of(page, size, sort, SORT, "createdAt");
        return PageResponse.of(sims.findAll(spec, pageable).map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public SimulationDetail getDetail(Integer id, User current) {
        return toDetail(findOwned(id, current));
    }

    @Transactional
    public void delete(Integer id, User current) {
        sims.delete(findOwned(id, current));
    }

    public byte[] downloadPdf(Integer id, User current) {
        SimulationDetail d = getDetail(id, current);
        byte[] bytes = pdf.build(d);
        pdf.register(id, current.getEmail(), bytes);
        return bytes;
    }

    public MessageResponse sendEmail(Integer id, User current) {
        SimulationDetail d = getDetail(id, current);
        byte[] bytes = pdf.build(d);
        boolean ok = mail.sendReport(current, current.getEmail(), d.neighborhoodName(),
                "oaxinvest-simulacion-" + id + ".pdf", bytes);
        if (!ok) {
            throw new ExternalServiceException("No se pudo enviar el correo. Intenta de nuevo en unos minutos");
        }
        pdf.register(id, current.getEmail(), bytes);
        return new MessageResponse("Reporte enviado a " + current.getEmail());
    }

    public MessageResponse sendSms(Integer id, User current, String phoneOverride) {
        SimulationDetail d = getDetail(id, current);
        String to = resolvePhone(current, phoneOverride);
        notifications.sendSms(current, to, shortText(d));
        return new MessageResponse("SMS enviado a " + to);
    }

    public MessageResponse sendWhatsapp(Integer id, User current, String phoneOverride) {
        SimulationDetail d = getDetail(id, current);
        String to = resolvePhone(current, phoneOverride);
        notifications.sendWhatsapp(current, to, "*OaxInvest* 🏢\n" + shortText(d));
        return new MessageResponse("WhatsApp enviado a " + to);
    }

    private String resolvePhone(User current, String override) {
        String phone = override != null && !override.isBlank() ? override : current.getPhone();
        if (phone == null || phone.isBlank()) {
            throw new BusinessException("Agrega tu teléfono en tu perfil o envíalo en la petición");
        }
        return phone;
    }

    private String shortText(SimulationDetail d) {
        return "Simulación en " + d.neighborhoodName() + ": semáforo " + d.semaphore() + ", ROI " + d.roi()
                + "% y recuperación en " + d.paybackMonths() + " meses. Detalle: " + frontendUrl + "/simulations/" + d.id();
    }

    private Simulation findOwned(Integer id, User current) {
        Simulation s = sims.findDetailedById(id).orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada"));
        if (!CurrentUser.isAdmin(current) && !s.getUser().getId().equals(current.getId())) {
            throw new ResourceNotFoundException("Simulación no encontrada");
        }
        return s;
    }

    private SimulationSummary toSummary(Simulation s) {
        return new SimulationSummary(s.getId(), s.getUser().getId(), s.getUser().getName(), s.getUser().getEmail(),
                s.getNeighborhood().getName(), s.getTotalArea(), s.getNumApartments(), s.getCalculatedRoi(),
                s.getCalculatedPaybackMonths(), s.getStatusSemaphore(), s.getCreatedAt());
    }

    private SimulationDetail toDetail(Simulation s) {
        List<AmenityResponse> am = s.getAmenities().stream()
                .sorted(Comparator.comparing(Amenity::getName)).map(AmenityResponse::from).toList();
        return new SimulationDetail(s.getId(), s.getUser().getId(), s.getUser().getName(), s.getNeighborhood().getId(),
                s.getNeighborhood().getName(), s.getLandWidth(), s.getLandLength(), s.getTotalArea(),
                s.getNumApartments(), s.getConstructionCostPerM2(), s.getMonthlyRentPerApartment(),
                s.getFixedMonthlyExpenses(), s.getCalculatedTotalCost(), s.getCalculatedAnnualIncome(),
                s.getCalculatedRoi(), s.getCalculatedPaybackMonths(), s.getStatusSemaphore(), s.getAiAnalysisText(),
                am, s.getCreatedAt());
    }
}
