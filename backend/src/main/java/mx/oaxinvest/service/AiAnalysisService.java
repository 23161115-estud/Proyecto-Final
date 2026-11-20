package mx.oaxinvest.service;

import com.fasterxml.jackson.databind.JsonNode;
import mx.oaxinvest.model.Semaphore;
import mx.oaxinvest.repository.AiTrainingDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);

    private final AiTrainingDataRepository dataset;
    private final String apiKey;
    private final String model;
    private final RestClient client;

    public AiAnalysisService(AiTrainingDataRepository dataset, @Value("${app.openai.api-key:}") String apiKey,
                             @Value("${app.openai.model:gpt-4o-mini}") String model) {
        this.dataset = dataset;
        this.apiKey = apiKey;
        this.model = model;
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000);
        f.setReadTimeout(20000);
        this.client = RestClient.builder().baseUrl("https://api.openai.com/v1").requestFactory(f).build();
    }

    public String analyze(String neighborhood, int apartments, BigDecimal rent, BigDecimal roi,
                          BigDecimal payback, Semaphore semaphore) {
        Double marketAvg = dataset.averageRentByNeighborhood(neighborhood);
        String local = localAnalysis(neighborhood, apartments, rent, roi, payback, semaphore, marketAvg);
        if (apiKey == null || apiKey.isBlank()) {
            return local;
        }
        try {
            String prompt = "Colonia: " + neighborhood + " (Oaxaca de Juárez). Departamentos: " + apartments
                    + ". Renta propuesta por departamento: $" + rent + " MXN/mes. "
                    + (marketAvg != null ? "Renta real promedio del dataset local: $" + Math.round(marketAvg) + " MXN. " : "")
                    + "ROI anual calculado: " + roi + "%. Recuperación: " + payback + " meses. Semáforo: " + semaphore
                    + ". Escribe un análisis de máximo 3 oraciones en español: viabilidad, riesgo principal y una recomendación.";
            Map<String, Object> body = Map.of("model", model, "max_tokens", 300, "temperature", 0.4,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "Eres un analista inmobiliario breve y claro para inversionistas en Oaxaca, México."),
                            Map.of("role", "user", "content", prompt)));
            JsonNode res = client.post().uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body).retrieve().body(JsonNode.class);
            String text = res == null ? "" : res.path("choices").path(0).path("message").path("content").asText("").trim();
            return text.isEmpty() ? local : text;
        } catch (Exception e) {
            log.warn("OpenAI no respondió ({}). Se usa el análisis local.", e.getMessage());
            return local;
        }
    }

    private String localAnalysis(String neighborhood, int apartments, BigDecimal rent, BigDecimal roi,
                                 BigDecimal payback, Semaphore semaphore, Double marketAvg) {
        StringBuilder sb = new StringBuilder();
        switch (semaphore) {
            case VERDE -> sb.append("Rentabilidad alta en ").append(neighborhood).append(": ROI de ").append(roi)
                    .append("% y recuperación en ").append(payback).append(" meses. ");
            case AMARILLO -> sb.append("Rentabilidad media en ").append(neighborhood).append(" (ROI ").append(roi)
                    .append("%). Conviene revisar costos o subir la renta. ");
            case ROJO -> sb.append("Rentabilidad baja en ").append(neighborhood).append(" (ROI ").append(roi)
                    .append("%). Con estos datos el proyecto no se recomienda. ");
        }
        if (marketAvg != null) {
            long avg = Math.round(marketAvg);
            if (rent.doubleValue() > avg * 1.25) {
                sb.append("Tu renta propuesta ($").append(rent.longValue()).append(") supera por mucho la renta real promedio de la zona ($")
                        .append(avg).append("), lo que puede alargar la ocupación.");
            } else {
                sb.append("La renta real promedio registrada en la zona es de $").append(avg).append(" mensuales.");
            }
        } else {
            sb.append("Aún no hay rentas reales registradas de esta colonia para comparar.");
        }
        return sb.toString();
    }
}
