package com.cajica.stream.controllers;

import com.cajica.stream.dto.CursoDTO;
import com.cajica.stream.entities.Usuario;
import com.cajica.stream.services.UsuarioService;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cursos")
public class CursoRestController {

  private final UsuarioService usuarioService;
  private static final Logger logger = LoggerFactory.getLogger(CursoRestController.class);

  @Autowired
  public CursoRestController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GetMapping("/mis-cursos")
  public ResponseEntity<List<CursoDTO>> getMisCursos() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
      logger.warn("Intento de acceder a mis cursos sin autenticación");
      return ResponseEntity.ok(Collections.emptyList());
    }

    String username = auth.getName();
    logger.info("Obteniendo cursos inscritos para el usuario: {}", username);

    try {
      // Obtener el usuario
      Optional<Usuario> usuarioOpt = usuarioService.findByUsername(username);
      if (usuarioOpt.isEmpty()) {
        logger.warn("No se encontró el usuario con username: {}", username);
        return ResponseEntity.ok(Collections.emptyList());
      }

      Usuario usuario = usuarioOpt.get();
      logger.info("Usuario encontrado con ID: {}", usuario.getId());

      // Obtener los cursos inscritos directamente de la base de datos
      List<Map<String, Object>> cursosData = usuarioService.getCursosInscritosRaw(usuario.getId());
      logger.info("Cursos inscritos recuperados: {}", cursosData.size());

      // Convertir los datos a DTOs
      List<CursoDTO> cursosDTO =
          cursosData.stream()
              .map(
                  data -> {
                    CursoDTO dto = new CursoDTO();
                    dto.setId(((Number) data.get("id")).longValue());
                    dto.setNombre((String) data.get("nombre"));
                    dto.setDescripcion((String) data.get("descripcion"));
                    dto.setCategoria((String) data.get("categoria"));
                    dto.setImagenPath((String) data.get("imagen_path"));

                    logger.info(
                        "Curso encontrado - ID: {}, Nombre: {}", dto.getId(), dto.getNombre());
                    return dto;
                  })
              .collect(Collectors.toList());

      return ResponseEntity.ok(cursosDTO);
    } catch (Exception e) {
      logger.error("Error al obtener cursos inscritos: {}", e.getMessage(), e);
      return ResponseEntity.ok(Collections.emptyList());
    }
  }
}
