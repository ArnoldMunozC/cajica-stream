package com.cajica.stream.controllers;

import com.cajica.stream.services.ConfiguracionSistemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracion")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracionController {

  private final ConfiguracionSistemaService configuracionService;

  @Autowired
  public AdminConfiguracionController(ConfiguracionSistemaService configuracionService) {
    this.configuracionService = configuracionService;
  }

  @GetMapping("/manual")
  public String verManual() {
    return "admin/manual";
  }

  @GetMapping("/diploma")
  public String verConfiguracionDiploma(Model model) {
    model.addAttribute("emisorNombre", configuracionService.getEmisorNombre());
    model.addAttribute("emisorCargo", configuracionService.getEmisorCargo());
    return "admin/configuracion-diploma";
  }

  @PostMapping("/diploma")
  public String guardarConfiguracionDiploma(
      @RequestParam("emisorNombre") String emisorNombre,
      @RequestParam("emisorCargo") String emisorCargo,
      RedirectAttributes redirectAttributes) {

    configuracionService.guardarEmisor(emisorNombre, emisorCargo);
    redirectAttributes.addFlashAttribute(
        "mensaje", "Configuración del diploma guardada exitosamente.");
    return "redirect:/admin/configuracion/diploma";
  }
}
