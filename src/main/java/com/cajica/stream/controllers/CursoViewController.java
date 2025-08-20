package com.cajica.stream.controllers;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.Usuario;
import com.cajica.stream.services.CursoService;
import com.cajica.stream.services.FileStorageService;
import com.cajica.stream.services.UsuarioService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cursos")
public class CursoViewController {

  private final CursoService cursoService;
  private final FileStorageService fileStorageService;
  private final UsuarioService usuarioService;
  private static final Logger logger = LoggerFactory.getLogger(CursoViewController.class);

  @Autowired
  public CursoViewController(
      CursoService cursoService,
      FileStorageService fileStorageService,
      UsuarioService usuarioService) {
    this.cursoService = cursoService;
    this.fileStorageService = fileStorageService;
    this.usuarioService = usuarioService;
  }

  @GetMapping
  public String listarCursos(Model model) {
    List<Curso> cursos = cursoService.findAll();
    model.addAttribute("cursos", cursos);
    return "cursos/lista";
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public String verCurso(@PathVariable Long id, Model model) {
    Optional<Curso> curso = cursoService.findById(id);
    if (curso.isPresent()) {
      model.addAttribute("curso", curso.get());

      // Verificar si el usuario está autenticado
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.findByUsername(username);

        if (usuarioOpt.isPresent()) {
          Usuario usuario = usuarioOpt.get();

          try {
            // Usar el método seguro para verificar inscripción
            boolean inscrito = usuarioService.verificarInscripcionSegura(username, id);
            model.addAttribute("inscrito", inscrito);
          } catch (Exception e) {
            // En caso de error, asumir que no está inscrito
            model.addAttribute("inscrito", false);
          }
        }
      }

      return "cursos/detalle";
    } else {
      return "redirect:/cursos";
    }
  }

  @PostMapping("/{id}/inscribir")
  public String inscribirEnCurso(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
      String username = auth.getName();
      Optional<Usuario> usuarioOpt = usuarioService.findByUsername(username);

      if (usuarioOpt.isPresent()) {
        Usuario usuario = usuarioOpt.get();
        try {
          // Usar el nuevo método JDBC para inscribir al usuario
          usuarioService.inscribirEnCursoJdbc(usuario.getId(), id);
          redirectAttributes.addFlashAttribute(
              "mensaje", "Te has inscrito exitosamente en el curso.");
        } catch (Exception e) {
          // Manejar la excepción y mostrar un mensaje de error al usuario
          redirectAttributes.addFlashAttribute(
              "error", "No se pudo completar la inscripción: " + e.getMessage());
        }
      }
    } else {
      redirectAttributes.addFlashAttribute(
          "error", "Necesitas iniciar sesión para inscribirte en un curso.");
      return "redirect:/login";
    }

    return "redirect:/cursos/" + id;
  }

  @PostMapping("/{id}/cancelar")
  public String cancelarInscripcion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    logger.info("Recibida solicitud para cancelar inscripción al curso ID: {}", id);
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
      String username = auth.getName();
      logger.info("Usuario autenticado: {}", username);
      Optional<Usuario> usuarioOpt = usuarioService.findByUsername(username);

      if (usuarioOpt.isPresent()) {
        Usuario usuario = usuarioOpt.get();
        logger.info(
            "Intentando cancelar inscripción del usuario ID: {} al curso ID: {}",
            usuario.getId(),
            id);
        try {
          usuarioService.cancelarInscripcion(usuario.getId(), id);
          logger.info("Inscripción cancelada exitosamente");
          redirectAttributes.addFlashAttribute("mensaje", "Has cancelado tu inscripción al curso.");
        } catch (Exception e) {
          logger.error("Error al cancelar inscripción: {}", e.getMessage(), e);
          logger.error("Tipo de excepción: {}", e.getClass().getName());
          logger.error("Stack trace: ", e);
          redirectAttributes.addFlashAttribute(
              "error",
              "Ocurrió un error al cancelar tu inscripción. Por favor, intenta de nuevo más"
                  + " tarde.");
        }
      } else {
        logger.warn("No se encontró el usuario con username: {}", username);
      }
    } else {
      logger.warn("Intento de cancelar inscripción sin usuario autenticado");
    }

    return "redirect:/cursos/" + id;
  }

  @GetMapping("/nuevo")
  @PreAuthorize("hasRole('ADMIN')")
  public String mostrarFormularioNuevo(Model model) {
    model.addAttribute("curso", new Curso());
    model.addAttribute("accion", "crear");
    return "cursos/formulario";
  }

  @PostMapping("/nuevo")
  @PreAuthorize("hasRole('ADMIN')")
  public String crearCurso(
      @ModelAttribute Curso curso,
      @RequestParam(value = "imagen", required = false) MultipartFile imagen,
      RedirectAttributes redirectAttributes) {

    Curso nuevoCurso = cursoService.save(curso, imagen);
    redirectAttributes.addFlashAttribute("mensaje", "Curso creado exitosamente");
    return "redirect:/cursos/" + nuevoCurso.getId();
  }

  @GetMapping("/editar/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
    Optional<Curso> curso = cursoService.findById(id);
    if (curso.isPresent()) {
      model.addAttribute("curso", curso.get());
      model.addAttribute("accion", "editar");
      return "cursos/formulario";
    } else {
      return "redirect:/cursos";
    }
  }

  @PostMapping("/editar/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public String actualizarCurso(
      @PathVariable Long id,
      @ModelAttribute Curso curso,
      @RequestParam(value = "imagen", required = false) MultipartFile imagen,
      RedirectAttributes redirectAttributes) {

    Curso cursoActualizado = cursoService.update(id, curso, imagen);
    if (cursoActualizado != null) {
      redirectAttributes.addFlashAttribute("mensaje", "Curso actualizado exitosamente");
      return "redirect:/cursos/" + cursoActualizado.getId();
    } else {
      return "redirect:/cursos";
    }
  }

  @GetMapping("/eliminar/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public String eliminarCurso(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    Optional<Curso> curso = cursoService.findById(id);
    if (curso.isPresent()) {
      boolean deshabilitado = cursoService.disableCurso(id);
      if (deshabilitado) {
        redirectAttributes.addFlashAttribute("mensaje", "Curso deshabilitado exitosamente");
      } else {
        redirectAttributes.addFlashAttribute("error", "No se pudo deshabilitar el curso");
      }
    } else {
      redirectAttributes.addFlashAttribute("error", "Curso no encontrado");
    }
    return "redirect:/cursos";
  }

  @GetMapping("/habilitar/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public String habilitarCurso(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    Optional<Curso> curso = cursoService.findById(id);
    if (curso.isPresent()) {
      boolean habilitado = cursoService.enableCurso(id);
      if (habilitado) {
        redirectAttributes.addFlashAttribute("mensaje", "Curso habilitado exitosamente");
      } else {
        redirectAttributes.addFlashAttribute("error", "No se pudo habilitar el curso");
      }
    } else {
      redirectAttributes.addFlashAttribute("error", "Curso no encontrado");
    }
    return "redirect:/cursos";
  }

  @GetMapping("/admin/deshabilitados")
  @PreAuthorize("hasRole('ADMIN')")
  public String listarCursosDeshabilitados(Model model) {
    List<Curso> todosLosCursos = cursoService.findAllIncludingDisabled();
    List<Curso> cursosDeshabilitados =
        todosLosCursos.stream().filter(curso -> !curso.isActivo()).collect(Collectors.toList());
    model.addAttribute("cursos", cursosDeshabilitados);
    model.addAttribute("titulo", "Cursos Deshabilitados");
    model.addAttribute("mostrandoDeshabilitados", true);
    return "cursos/lista";
  }
}
