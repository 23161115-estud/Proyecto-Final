package mx.oaxinvest.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import mx.oaxinvest.exception.BusinessException;
import mx.oaxinvest.exception.ExternalServiceException;
import mx.oaxinvest.model.Channel;
import mx.oaxinvest.model.NotificationLog;
import mx.oaxinvest.model.SendStatus;
import mx.oaxinvest.model.User;
import mx.oaxinvest.repository.NotificationLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationLogRepository logs;
    private final String sid;
    private final String token;
    private final String smsFrom;
    private final String whatsappFrom;
    private final String whatsappProvider;
    private final String metaToken;
    private final String metaPhoneId;
    private final String metaVersion;
    private final String metaTemplate;
    private final String metaTemplateLang;
    private final boolean metaMxAddOne;
    private final RestClient http;
    private boolean configured;

    public NotificationService(NotificationLogRepository logs,
                               @Value("${app.twilio.account-sid:}") String sid,
                               @Value("${app.twilio.auth-token:}") String token,
                               @Value("${app.twilio.sms-from:}") String smsFrom,
                               @Value("${app.twilio.whatsapp-from:}") String whatsappFrom,
                               @Value("${app.whatsapp.provider:twilio}") String whatsappProvider,
                               @Value("${app.meta.whatsapp.token:}") String metaToken,
                               @Value("${app.meta.whatsapp.phone-number-id:}") String metaPhoneId,
                               @Value("${app.meta.whatsapp.api-version:v21.0}") String metaVersion,
                               @Value("${app.meta.whatsapp.template:}") String metaTemplate,
                               @Value("${app.meta.whatsapp.template-lang:en_US}") String metaTemplateLang,
                               @Value("${app.meta.whatsapp.mx-add-1:false}") boolean metaMxAddOne) {
        this.logs = logs;
        this.sid = sid.trim();
        this.token = token.trim();
        this.smsFrom = smsFrom.trim();
        this.whatsappFrom = whatsappFrom.trim();
        this.whatsappProvider = whatsappProvider.trim().toLowerCase();
        this.metaToken = metaToken.trim();
        this.metaPhoneId = metaPhoneId.trim();
        this.metaVersion = metaVersion.trim();
        this.metaTemplate = metaTemplate.trim();
        this.metaTemplateLang = metaTemplateLang.trim();
        this.metaMxAddOne = metaMxAddOne;
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000);
        f.setReadTimeout(15000);
        this.http = RestClient.builder().requestFactory(f).build();
    }

    @PostConstruct
    void init() {
        if (!sid.isBlank() && !token.isBlank()) {
            Twilio.init(sid, token);
            configured = true;
        } else {
            log.warn("Twilio no está configurado: el SMS (y WhatsApp si el proveedor es twilio) responderá 422 hasta que se llenen las credenciales");
        }
    }

    public void sendSms(User user, String to, String body) {
        if (!configured || smsFrom.isBlank()) {
            throw new BusinessException("El envío de SMS no está configurado en el servidor");
        }
        dispatch(user, Channel.SMS, to, () -> Message.creator(new PhoneNumber(to), new PhoneNumber(smsFrom), body).create());
    }

    public void sendWhatsapp(User user, String to, String body) {
        if (whatsappProvider.equals("meta")) {
            sendWhatsappMeta(user, to, body);
            return;
        }
        if (!configured || whatsappFrom.isBlank()) {
            throw new BusinessException("El envío de WhatsApp no está configurado en el servidor");
        }
        dispatch(user, Channel.WHATSAPP, to, () -> Message.creator(new PhoneNumber("whatsapp:" + to),
                new PhoneNumber(whatsappFrom), body).create());
    }

    private void sendWhatsappMeta(User user, String to, String body) {
        if (metaToken.isBlank() || metaPhoneId.isBlank()) {
            throw new BusinessException("El envío de WhatsApp no está configurado en el servidor");
        }
        String digits = to.replaceAll("\\D", "");
        if (metaMxAddOne && digits.startsWith("52") && digits.length() == 12) {
            digits = "521" + digits.substring(2);
        }
        final String recipient = digits;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", recipient);
        if (metaTemplate.isBlank()) {
            payload.put("type", "text");
            payload.put("text", Map.of("body", body, "preview_url", false));
        } else {
            payload.put("type", "template");
            payload.put("template", Map.of("name", metaTemplate, "language", Map.of("code", metaTemplateLang)));
        }
        String uri = "https://graph.facebook.com/" + metaVersion + "/" + metaPhoneId + "/messages";
        NotificationLog entry = new NotificationLog();
        entry.setUserId(user.getId());
        entry.setChannel(Channel.WHATSAPP);
        entry.setRecipient(to);
        try {
            JsonNode res = http.post().uri(uri)
                    .header("Authorization", "Bearer " + metaToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
            String id = res == null ? "" : res.path("messages").path(0).path("id").asText("");
            entry.setStatus(SendStatus.SENT);
            entry.setProviderSid(id.length() > 64 ? id.substring(0, 64) : id);
            logs.save(entry);
        } catch (RestClientResponseException e) {
            String detail = e.getResponseBodyAsString();
            fail(entry, "Meta " + e.getStatusCode().value() + ": " + detail);
        } catch (Exception e) {
            fail(entry, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }

    private void fail(NotificationLog entry, String msg) {
        entry.setStatus(SendStatus.FAILED);
        entry.setErrorMessage(msg.length() > 500 ? msg.substring(0, 500) : msg);
        logs.save(entry);
        log.error("WhatsApp falló: {}", msg);
        throw new ExternalServiceException("No se pudo enviar el mensaje por whatsapp. Verifica el número y que tu cuenta de WhatsApp pueda escribirle");
    }

    private void dispatch(User user, Channel channel, String to, java.util.function.Supplier<Message> action) {
        NotificationLog entry = new NotificationLog();
        entry.setUserId(user.getId());
        entry.setChannel(channel);
        entry.setRecipient(to);
        try {
            Message m = action.get();
            entry.setStatus(SendStatus.SENT);
            entry.setProviderSid(m.getSid());
            logs.save(entry);
        } catch (Exception e) {
            entry.setStatus(SendStatus.FAILED);
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            entry.setErrorMessage(msg.length() > 500 ? msg.substring(0, 500) : msg);
            logs.save(entry);
            log.error("Twilio {} falló: {}", channel, msg);
            throw new ExternalServiceException("No se pudo enviar el mensaje por " + channel.name().toLowerCase()
                    + ". Verifica el número y que tu cuenta de Twilio pueda escribirle");
        }
    }
}
