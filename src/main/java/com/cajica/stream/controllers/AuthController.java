package com.cajica.stream.controllers;

import com.cajica.stream.entities.Usuario;
import com.cajica.stream.services.UsuarioService;
import jakarta.validation.Valid;
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

  private final UsuarioService usuarioService;

  @Autowired
  public AuthController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GetMapping("/login")
  public String mostrarFormularioLogin() {
    return "auth/login";
  }

  @GetMapping("/registro")
  public String mostrarFormularioRegistro(Model model) {
    model.addAttribute("usuario", new Usuario());
    return "auth/registro";
  }

  @PostMapping("/registro")
  public String registrarUsuario(
      @Valid @ModelAttribute("usuario") Usuario usuario,
      BindingResult result,
      RedirectAttributes redirectAttributes) {

    // Validar si el usuario ya existe
    if (usuarioService.existeUsuario(usuario.getUsername(), usuario.getEmail())) {
      result.rejectValue(
          "username", "error.usuario", "El nombre de usuario o email ya está en uso");
    }

    if (result.hasErrors()) {
      return "auth/registro";
    }

    usuarioService.registrarUsuario(usuario);
    redirectAttributes.addFlashAttribute(
        "mensaje", "¡Registro exitoso! Ahora puedes iniciar sesión.");

    return "redirect:/login";
  }
}
