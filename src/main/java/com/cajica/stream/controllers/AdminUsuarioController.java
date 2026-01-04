package com.cajica.stream.controllers;

import com.cajica.stream.entities.Usuario;
import com.cajica.stream.services.UsuarioService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

  private final UsuarioService usuarioService;

  public AdminUsuarioController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public String listarUsuarios(@RequestParam(value = "q", required = false) String q, Model model) {
    List<Usuario> usuarios = usuarioService.buscarPorNombreCompleto(q);
    model.addAttribute("usuarios", usuarios);
    model.addAttribute("q", q);
    return "admin/usuarios/lista";
  }

  @PostMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public String actualizarUsuario(
      @PathVariable("id") Long id,
      @RequestParam("email") String email,
      @RequestParam(value = "activo", defaultValue = "false") boolean activo,
      @RequestParam(value = "q", required = false) String q,
      RedirectAttributes redirectAttributes) {
    try {
      usuarioService.actualizarEmailYActivo(id, email, activo);
      redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }

    if (q != null && !q.isBlank()) {
      return "redirect:/admin/usuarios?q=" + q;
    }
    return "redirect:/admin/usuarios";
  }
}
