package com.cajica.stream.controllers;

import com.cajica.stream.services.UsuarioService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionRestController {

  private final UsuarioService usuarioService;
  private static final Logger logger = LoggerFactory.getLogger(InscripcionRestController.class);

  @Autowired
  public InscripcionRestController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  /**
   * Endpoint para verificar si un usuario está inscrito en un curso específico. Este endpoint se
   * llama de manera asíncrona desde el frontend para evitar problemas de concurrencia durante la
   * carga de la página.
   *
   * @param cursoId ID del curso a verificar
   * @return ResponseEntity con un mapa que contiene el estado de inscripción
   */
  @GetMapping("/verificar/{cursoId}")
  public ResponseEntity<Map<String, Object>> verificarInscripcion(@PathVariable Long cursoId) {
    logger.info("Recibida solicitud para verificar inscripción en curso ID: {}", cursoId);
    Map<String, Object> response = new HashMap<>();

    // Obtener el usuario autenticado
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
      String username = auth.getName();
      logger.info("Usuario autenticado: {}", username);

      try {
        // Verificar inscripción de manera segura
        logger.debug("Verificando inscripción para usuario {} en curso {}", username, cursoId);
        boolean inscrito = usuarioService.verificarInscripcionSegura(username, cursoId);

        logger.info(
            "Resultado de verificación: usuario {} {} inscrito en curso {}",
            username,
            inscrito ? "está" : "no está",
            cursoId);

        response.put("inscrito", inscrito);
        response.put("success", true);
        return ResponseEntity.ok(response);
      } catch (Exception e) {
        // En caso de error, devolver false por defecto
        logger.error("Error al verificar inscripción: {}", e.getMessage(), e);
        response.put("inscrito", false);
        response.put("success", false);
        response.put("error", "Error al verificar inscripción: " + e.getMessage());
        return ResponseEntity.ok(response);
      }
    }

    // Si no hay usuario autenticado, devolver false
    logger.warn("Intento de verificar inscripción sin usuario autenticado");
    response.put("inscrito", false);
    response.put("success", false);
    response.put("error", "Usuario no autenticado");
    return ResponseEntity.ok(response);
  }
}
