package com.cajica.stream.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${app.base-url:http://localhost:8082}")
  private String baseUrl;

  @Autowired
  public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
    this.mailSender = mailSender;
    this.templateEngine = templateEngine;
    logger.info("EmailService inicializado con baseUrl: {}", baseUrl);
  }

  /**
   * Envía un correo electrónico para recuperación de contraseña
   *
   * @param to Dirección de correo del destinatario
   * @param token Token de recuperación
   * @param username Nombre de usuario
   * @return true si el correo se envió correctamente, false en caso contrario
   */
  public boolean enviarCorreoRecuperacion(String to, String token, String username) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject("Recuperación de Contraseña - Cajica Stream");

      // Crear contexto para la plantilla
      Context context = new Context();
      context.setVariable("username", username);
      String resetUrl = baseUrl + "/password/reset?token=" + token;
      context.setVariable("resetUrl", resetUrl);
      context.setVariable("expiryHours", 24);

      logger.info("Generando enlace de recuperación: {}", resetUrl);

      // Procesar la plantilla HTML
      String emailContent = templateEngine.process("email/password-recovery", context);
      helper.setText(emailContent, true);

      logger.info("Enviando correo de recuperación a: {} con token: {}", to, token);
      mailSender.send(message);
      logger.info("Correo de recuperación enviado a: {}", to);
      return true;
    } catch (MessagingException e) {
      logger.error("Error al enviar correo de recuperación: {}", e.getMessage(), e);
      return false;
    }
  }
}
