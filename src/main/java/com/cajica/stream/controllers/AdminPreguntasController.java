package com.cajica.stream.controllers;

import com.cajica.stream.entities.VideoPregunta;
import com.cajica.stream.services.VideoQAService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/preguntas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPreguntasController {

  private final VideoQAService videoQAService;

  public AdminPreguntasController(VideoQAService videoQAService) {
    this.videoQAService = videoQAService;
  }

  @GetMapping
  public String listarPendientes(Model model) {
    List<VideoPregunta> pendientes = videoQAService.findPreguntasPendientes();
    long count = videoQAService.countPreguntasPendientes();
    model.addAttribute("pendientes", pendientes);
    model.addAttribute("pendientesCount", count);
    return "admin/preguntas/lista";
  }
}
