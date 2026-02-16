package com.cajica.stream.controllers;

import com.cajica.stream.entities.Usuario;
import com.cajica.stream.services.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final UsuarioService usuarioService;

  @Autowired
  public AuthController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GetMapping("/login")
  public String mostrarFormularioLogin() {
    logger.debug("Mostrando formulario de login");
    return "auth/login";
  }

  @GetMapping("/registro")
  public String mostrarFormularioRegistro(Model model) {
    logger.debug("Mostrando formulario de registro");
    model.addAttribute("usuario", new Usuario());
    return "auth/registro";
  }

  @PostMapping("/registro")
  public String registrarUsuario(
      @Valid @ModelAttribute("usuario") Usuario usuario,
      BindingResult result,
      @org.springframework.web.bind.annotation.RequestParam("confirmPassword") String confirmPassword,
      RedirectAttributes redirectAttributes) {

    logger.info(
        "Iniciando registro de usuario. username={}, email={}",
        usuario.getUsername(),
        usuario.getEmail());

    // Validar que las contraseñas coincidan
    if (!usuario.getPassword().equals(confirmPassword)) {
      logger.warn(
          "Registro rechazado: contraseñas no coinciden. username={}, email={}",
          usuario.getUsername(),
          usuario.getEmail());
      result.rejectValue("password", "error.usuario", "Las contraseñas no coinciden");
    }

    // Validar si el usuario ya existe
    if (usuarioService.existeUsuario(usuario.getUsername(), usuario.getEmail())) {
      logger.warn(
          "Registro rechazado: usuario/email ya existe. username={}, email={}",
          usuario.getUsername(),
          usuario.getEmail());
      result.rejectValue(
          "username", "error.usuario", "El nombre de usuario o email ya está en uso");
    }

    if (result.hasErrors()) {
      logger.info(
          "Registro con errores de validación. username={}, email={}",
          usuario.getUsername(),
          usuario.getEmail());
      return "auth/registro";
    }

    usuarioService.registrarUsuario(usuario);
    logger.info(
        "Registro completado exitosamente. username={}, email={}",
        usuario.getUsername(),
        usuario.getEmail());
    redirectAttributes.addFlashAttribute(
        "mensaje", "¡Registro exitoso! Ahora puedes iniciar sesión.");

    return "redirect:/login";
  }
}
