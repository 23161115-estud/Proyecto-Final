package mx.oaxinvest.config;

import mx.oaxinvest.model.*;
import mx.oaxinvest.repository.*;
import mx.oaxinvest.service.SimulationCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final RoleRepository roles;
    private final NeighborhoodRepository neighborhoods;
    private final AmenityRepository amenities;
    private final SimulationRepository simulations;
    private final EmailLogRepository emailLogs;
    private final SimulationCalculator calculator;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, RoleRepository roles, NeighborhoodRepository neighborhoods,
                      AmenityRepository amenities, SimulationRepository simulations, EmailLogRepository emailLogs,
                      SimulationCalculator calculator, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.neighborhoods = neighborhoods;
        this.amenities = amenities;
        this.simulations = simulations;
        this.emailLogs = emailLogs;
        this.calculator = calculator;
        this.encoder = encoder;
    }

    private record U(String name, String email, AuthProvider provider, boolean verified, String role, String password, String phone) {
    }

    private record S(String email, String neighborhood, String w, String l, int apts, String cost, String rent,
                     String fixed, String[] amenities, String ai) {
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        Map<String, Role> roleMap = new HashMap<>();
        roles.findAll().forEach(r -> roleMap.put(r.getName(), r));

        List<U> seed = List.of(
                new U("Administrador", "admin@oaxinvest.mx", AuthProvider.LOCAL, true, "ADMIN", "Admin#2026", null),
                new U("Ana López", "ana@test.com", AuthProvider.LOCAL, true, "CLIENTE", "Cliente#2026", "+529511234567"),
                new U("Carlos Ruiz", "carlos@test.com", AuthProvider.GOOGLE, true, "CLIENTE", null, null),
                new U("María Paz", "maria@test.com", AuthProvider.LOCAL, false, "CLIENTE", "Cliente#2026", null),
                new U("Pedro Solis", "pedro@test.com", AuthProvider.LOCAL, true, "ANALISTA", "Analista#2026", null),
                new U("Luis Miguel", "luis@test.com", AuthProvider.FACEBOOK, true, "CLIENTE", null, null),
                new U("Elena Poniatowska", "elena@test.com", AuthProvider.LOCAL, true, "ANALISTA", "Analista#2026", null),
                new U("Jorge Pérez", "jorge@test.com", AuthProvider.LOCAL, false, "CLIENTE", "Cliente#2026", null),
                new U("Sofía Castro", "sofia@test.com", AuthProvider.LOCAL, true, "CLIENTE", "Cliente#2026", null),
                new U("Diego Rivera", "diego@test.com", AuthProvider.GOOGLE, true, "CLIENTE", null, null),
                new U("Frida Kahlo", "frida@test.com", AuthProvider.LOCAL, true, "CLIENTE", "Cliente#2026", null),
                new U("Benito Juárez", "benito@test.com", AuthProvider.LOCAL, true, "CLIENTE", "Cliente#2026", null),
                new U("Porfirio Díaz", "porfirio@test.com", AuthProvider.GOOGLE, false, "CLIENTE", null, null));

        Map<String, User> byEmail = new HashMap<>();
        for (U u : seed) {
            User e = new User();
            e.setName(u.name());
            e.setEmail(u.email());
            e.setProvider(u.provider());
            e.setEmailVerified(u.verified());
            e.setRole(roleMap.get(u.role()));
            e.setPasswordHash(u.password() == null ? null : encoder.encode(u.password()));
            e.setPhone(u.phone());
            byEmail.put(u.email(), users.save(e));
        }

        Map<String, Neighborhood> nMap = new HashMap<>();
        neighborhoods.findAll().forEach(n -> nMap.put(n.getName(), n));
        Map<String, Amenity> aMap = new HashMap<>();
        amenities.findAll().forEach(a -> aMap.put(a.getName(), a));

        List<S> sims = List.of(
                new S("ana@test.com", "Oaxaca Centro", "10", "20", 10, "1500", "800", "1000", new String[]{"Alberca", "Estacionamiento"}, "Alta demanda estudiantil y turística en el centro."),
                new S("carlos@test.com", "Reforma", "12", "25", 12, "1450", "750", "1200", new String[]{"Gimnasio"}, null),
                new S("maria@test.com", "Xochimilco", "8", "15", 6, "1400", "600", "800", new String[]{"Seguridad 24/7"}, "Zona en desarrollo: conviene revisar la renta."),
                new S("ana@test.com", "La Paz", "10", "20", 10, "1450", "800", "1000", new String[]{"Alberca", "Cisterna"}, null),
                new S("luis@test.com", "San Felipe", "15", "30", 15, "1550", "700", "1500", new String[]{"Alberca", "Gimnasio", "Estacionamiento"}, null),
                new S("jorge@test.com", "Jalatlaco", "10", "20", 10, "1500", "650", "1000", new String[]{"Salón de Eventos"}, "Revisar costos de mantenimiento."),
                new S("sofia@test.com", "Santa María", "9", "18", 9, "1400", "500", "900", new String[]{"Área de Asadores"}, null),
                new S("diego@test.com", "Trinidad", "11", "22", 11, "1450", "750", "1100", new String[]{"Jardín"}, null),
                new S("frida@test.com", "Ex-Marquesado", "10", "20", 10, "1550", "850", "1000", new String[]{}, null),
                new S("benito@test.com", "Volcanes", "10", "20", 10, "1400", "600", "1000", new String[]{"Paneles Solares"}, "Zona con baja plusvalía."),
                new S("porfirio@test.com", "Amatlán", "10", "20", 10, "1400", "500", "1000", new String[]{}, null),
                new S("ana@test.com", "San Jacinto", "10", "20", 10, "1450", "400", "1000", new String[]{"Elevador"}, null));

        for (S s : sims) {
            Set<Amenity> chosen = new HashSet<>();
            for (String name : s.amenities()) {
                chosen.add(aMap.get(name));
            }
            SimulationCalculator.Result c = calculator.calculate(new BigDecimal(s.w()), new BigDecimal(s.l()), s.apts(),
                    new BigDecimal(s.cost()), new BigDecimal(s.rent()), new BigDecimal(s.fixed()), chosen);
            Simulation e = new Simulation();
            e.setUser(byEmail.get(s.email()));
            e.setNeighborhood(nMap.get(s.neighborhood()));
            e.setLandWidth(new BigDecimal(s.w()));
            e.setLandLength(new BigDecimal(s.l()));
            e.setTotalArea(c.totalArea());
            e.setNumApartments(s.apts());
            e.setConstructionCostPerM2(new BigDecimal(s.cost()));
            e.setMonthlyRentPerApartment(new BigDecimal(s.rent()));
            e.setFixedMonthlyExpenses(new BigDecimal(s.fixed()));
            e.setCalculatedTotalCost(c.totalCost());
            e.setCalculatedAnnualIncome(c.annualIncome());
            e.setCalculatedRoi(c.roi());
            e.setCalculatedPaybackMonths(c.paybackMonths());
            e.setStatusSemaphore(c.semaphore());
            e.setAiAnalysisText(s.ai());
            e.setAmenities(chosen);
            simulations.save(e);
        }

        Object[][] logs = {
                {"ana@test.com", EmailType.VERIFICATION, SendStatus.SENT}, {"carlos@test.com", EmailType.WELCOME, SendStatus.SENT},
                {"maria@test.com", EmailType.VERIFICATION, SendStatus.PENDING}, {"pedro@test.com", EmailType.PDF_REPORT, SendStatus.SENT},
                {"luis@test.com", EmailType.PDF_REPORT, SendStatus.SENT}, {"elena@test.com", EmailType.WELCOME, SendStatus.FAILED},
                {"jorge@test.com", EmailType.VERIFICATION, SendStatus.SENT}, {"sofia@test.com", EmailType.PDF_REPORT, SendStatus.SENT},
                {"diego@test.com", EmailType.WELCOME, SendStatus.SENT}, {"frida@test.com", EmailType.PDF_REPORT, SendStatus.SENT},
                {"benito@test.com", EmailType.VERIFICATION, SendStatus.SENT}, {"porfirio@test.com", EmailType.PDF_REPORT, SendStatus.PENDING}};
        for (Object[] l : logs) {
            EmailLog e = new EmailLog();
            e.setUserId(byEmail.get((String) l[0]).getId());
            e.setRecipientEmail((String) l[0]);
            e.setEmailType((EmailType) l[1]);
            e.setStatus((SendStatus) l[2]);
            if (l[2] == SendStatus.FAILED) {
                e.setErrorMessage("Buzón no disponible (dato de prueba)");
            }
            emailLogs.save(e);
        }
        log.info("Datos de prueba cargados: {} usuarios, {} simulaciones", seed.size(), sims.size());
    }
}
