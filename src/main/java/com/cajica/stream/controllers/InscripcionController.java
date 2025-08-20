package com.cajica.stream.controllers;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.Usuario;
import com.cajica.stream.services.CursoService;
import com.cajica.stream.services.UsuarioService;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InscripcionController {

  private final UsuarioService usuarioService;
  private final CursoService cursoService;
  private static final Logger logger = LoggerFactory.getLogger(InscripcionController.class);

  @Autowired
  public InscripcionController(UsuarioService usuarioService, CursoService cursoService) {
    this.usuarioService = usuarioService;
    this.cursoService = cursoService;
  }

  @GetMapping("/mis-cursos")
  public String misCursos(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();

    logger.info("Accediendo a Mis Cursos para el usuario: {}", username);

    Optional<Usuario> usuarioOpt = usuarioService.findByUsername(username);
    if (usuarioOpt.isPresent()) {
      Usuario usuario = usuarioOpt.get();
      logger.info("Usuario encontrado con ID: {}", usuario.getId());

      // Verificar directamente en la base de datos si hay inscripciones
      boolean tieneInscripciones = usuarioService.verificarSiTieneInscripciones(usuario.getId());
      logger.info("¿El usuario tiene inscripciones? {}", tieneInscripciones);

      // Usar el método directo para evitar ConcurrentModificationException
      Set<Curso> cursosInscritos = usuarioService.getCursosInscritosDirecto(usuario.getId());
      logger.info("Cursos inscritos recuperados: {}", cursosInscritos.size());

      // Imprimir detalles de cada curso para depuración
      for (Curso curso : cursosInscritos) {
        logger.info("Curso inscrito: ID={}, Nombre={}", curso.getId(), curso.getNombre());
      }

      model.addAttribute("cursos", cursosInscritos);
      return "cursos/mis-cursos";
    } else {
      logger.warn("No se encontró el usuario con username: {}", username);
    }

    return "redirect:/cursos";
  }
}
