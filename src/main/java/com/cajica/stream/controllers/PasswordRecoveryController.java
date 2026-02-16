package com.cajica.stream.controllers;

import com.cajica.stream.entities.Usuario;
import com.cajica.stream.services.EmailService;
import com.cajica.stream.services.SecurityRateLimiterService;
import com.cajica.stream.services.UsuarioService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/password")
public class PasswordRecoveryController {

  private static final Logger logger = LoggerFactory.getLogger(PasswordRecoveryController.class);
  private final UsuarioService usuarioService;
  private final EmailService emailService;
  private final SecurityRateLimiterService rateLimiterService;

  @Autowired
  public PasswordRecoveryController(
      UsuarioService usuarioService,
      EmailService emailService,
      SecurityRateLimiterService rateLimiterService) {
    this.usuarioService = usuarioService;
    this.emailService = emailService;
    this.rateLimiterService = rateLimiterService;
  }

  /** Muestra el formulario para solicitar recuperación de contraseña */
  @GetMapping("/forgot")
  public String mostrarFormularioRecuperacion() {
    return "auth/forgot-password";
  }

  /** Procesa la solicitud de recuperación de contraseña */
  @PostMapping("/forgot")
  public String procesarSolicitudRecuperacion(
      @RequestParam("email") String email, RedirectAttributes redirectAttributes) {
    logger.info("Procesando solicitud de recuperación para email: {}", email);

    // Verificar límite de intentos
    if (!rateLimiterService.allowRequest(email)) {
      logger.warn("Solicitud bloqueada por exceder límite de intentos: {}", email);
      redirectAttributes.addFlashAttribute(
          "error",
          "Has excedido el número máximo de intentos. Por favor, intenta nuevamente más tarde.");
      return "redirect:/password/forgot";
    }

    boolean resultado = usuarioService.generarTokenRecuperacion(email);

    if (resultado) {
      // Obtener el usuario para enviar el correo
      Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);
      if (usuarioOpt.isPresent()) {
        Usuario usuario = usuarioOpt.get();
        // Enviar correo electrónico con el enlace de recuperación
        boolean emailEnviado =
            emailService.enviarCorreoRecuperacion(
                email, usuario.getResetPasswordToken(), usuario.getUsername());

        if (emailEnviado) {
          logger.info("Correo de recuperación enviado exitosamente a: {}", email);
          // Registrar intento exitoso para reiniciar contador
          rateLimiterService.registerSuccessfulAttempt(email);
        } else {
          logger.warn("No se pudo enviar el correo de recuperación a: {}", email);
          // Aún así mostramos mensaje de éxito al usuario por seguridad
        }
      }

      redirectAttributes.addFlashAttribute(
          "mensaje",
          "Se ha enviado un enlace de recuperación a tu correo electrónico. "
              + "Por favor, revisa tu bandeja de entrada.");
    } else {
      // Por seguridad, no revelamos si el email existe o no
      redirectAttributes.addFlashAttribute(
          "mensaje",
          "Si tu correo electrónico está registrado, recibirás un enlace para restablecer tu"
              + " contraseña.");
    }

    return "redirect:/login";
  }

  /** Muestra el formulario para restablecer la contraseña */
  @GetMapping("/reset")
  public String mostrarFormularioRestablecimiento(
      @RequestParam("token") String token, Model model) {
    logger.info("Mostrando formulario de restablecimiento de contraseña");

    Optional<Usuario> usuarioOpt = usuarioService.validarTokenRecuperacion(token);

    if (usuarioOpt.isEmpty()) {
      logger.warn("Token inválido o expirado");
      model.addAttribute("error", "El enlace de recuperación es inválido o ha expirado.");
      return "auth/invalid-token";
    }

    model.addAttribute("token", token);
    return "auth/reset-password";
  }

  /** Procesa la solicitud de restablecimiento de contraseña */
  @PostMapping("/reset")
  public String procesarRestablecimiento(
      @RequestParam("token") String token,
      @RequestParam("password") String password,
      @RequestParam("confirmPassword") String confirmPassword,
      RedirectAttributes redirectAttributes) {
    logger.info("Procesando restablecimiento de contraseña");

    // Validar que las contraseñas coincidan
    if (!password.equals(confirmPassword)) {
      logger.warn("Las contraseñas no coinciden");
      redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
      return "redirect:/password/reset?token=" + token;
    }

    // Validar longitud mínima de contraseña
    if (password.length() < 6) {
      logger.warn("Contraseña demasiado corta");
      redirectAttributes.addFlashAttribute(
          "error", "La contraseña debe tener al menos 6 caracteres.");
      return "redirect:/password/reset?token=" + token;
    }

    boolean resultado = usuarioService.actualizarContraseña(token, password);

    if (resultado) {
      logger.info("Contraseña actualizada exitosamente");
      redirectAttributes.addFlashAttribute(
          "mensaje",
          "Tu contraseña ha sido actualizada exitosamente. Ahora puedes iniciar sesión con tu nueva"
              + " contraseña.");
      return "redirect:/login";
    } else {
      logger.warn("No se pudo actualizar la contraseña");
      redirectAttributes.addFlashAttribute(
          "error",
          "No se pudo actualizar la contraseña. El enlace puede ser inválido o haber expirado.");
      return "redirect:/password/forgot";
    }
  }
}
