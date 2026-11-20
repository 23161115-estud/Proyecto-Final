package mx.oaxinvest.service;

import jakarta.mail.internet.MimeMessage;
import mx.oaxinvest.model.EmailLog;
import mx.oaxinvest.model.EmailType;
import mx.oaxinvest.model.SendStatus;
import mx.oaxinvest.model.User;
import mx.oaxinvest.repository.EmailLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender sender;
    private final EmailLogRepository logs;
    private final String from;
    private final String frontendUrl;

    public MailService(JavaMailSender sender, EmailLogRepository logs,
                       @Value("${app.mail.from}") String from, @Value("${app.frontend-url}") String frontendUrl) {
        this.sender = sender;
        this.logs = logs;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    public boolean send(User user, EmailType type, String to, String subject, String htmlBody,
                        String attachmentName, byte[] attachment) {
        EmailLog entry = new EmailLog();
        entry.setUserId(user.getId());
        entry.setEmailType(type);
        entry.setRecipientEmail(to);
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, attachment != null, "UTF-8");
            helper.setFrom(from, "OaxInvest");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (attachment != null) {
                helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
            }
            sender.send(message);
            entry.setStatus(SendStatus.SENT);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}: {}", to, e.getMessage());
            entry.setStatus(SendStatus.FAILED);
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            entry.setErrorMessage(msg.length() > 500 ? msg.substring(0, 500) : msg);
        }
        logs.save(entry);
        return entry.getStatus() == SendStatus.SENT;
    }

    public boolean sendVerification(User user) {
        String link = frontendUrl + "/verify-email?token=" + user.getVerificationToken();
        String html = layout("Confirma tu correo",
                "<p>Hola " + escape(user.getName()) + ", gracias por registrarte en OaxInvest.</p>"
                        + "<p>Confirma tu correo con el botón de abajo:</p>" + button(link, "Confirmar correo"));
        return send(user, EmailType.VERIFICATION, user.getEmail(), "Confirma tu correo en OaxInvest", html, null, null);
    }

    public boolean sendWelcome(User user) {
        String html = layout("¡Bienvenido a OaxInvest!",
                "<p>Hola " + escape(user.getName()) + ", tu cuenta ya está lista.</p>"
                        + "<p>Simula la rentabilidad de tu próximo proyecto de departamentos en Oaxaca.</p>"
                        + button(frontendUrl, "Entrar a OaxInvest"));
        return send(user, EmailType.WELCOME, user.getEmail(), "Bienvenido a OaxInvest", html, null, null);
    }

    public boolean sendPasswordReset(User user, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String html = layout("Recupera tu contraseña",
                "<p>Recibimos una solicitud para cambiar tu contraseña. El enlace vence en 30 minutos.</p>"
                        + button(link, "Crear nueva contraseña")
                        + "<p style=\"color:#5E6B66\">Si no fuiste tú, ignora este mensaje.</p>");
        return send(user, EmailType.PASSWORD_RESET, user.getEmail(), "Recupera tu contraseña de OaxInvest", html, null, null);
    }

    public boolean sendReport(User user, String to, String neighborhood, String fileName, byte[] pdf) {
        String html = layout("Tu reporte de simulación",
                "<p>Adjuntamos el reporte PDF de tu simulación en <b>" + escape(neighborhood) + "</b>.</p>");
        return send(user, EmailType.PDF_REPORT, to, "Reporte de simulación: " + neighborhood, html, fileName, pdf);
    }

    private String button(String link, String label) {
        return "<p><a href=\"" + link + "\" style=\"background:#1D5E4B;color:#fff;padding:12px 22px;"
                + "border-radius:10px;text-decoration:none;font-weight:bold\">" + label + "</a></p>"
                + "<p style=\"color:#5E6B66;font-size:12px\">Si el botón no funciona, copia este enlace: " + link + "</p>";
    }

    private String layout(String title, String content) {
        return "<div style=\"font-family:Arial,sans-serif;max-width:560px;margin:auto;color:#1E2523\">"
                + "<h2 style=\"color:#1D5E4B\">OaxInvest</h2><h3>" + title + "</h3>" + content + "</div>";
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
