package com.cajica.stream.controllers;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.Video;
import com.cajica.stream.services.CursoService;
import com.cajica.stream.services.UsuarioService;
import com.cajica.stream.services.VideoService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VideoSecurityController {

  private final VideoService videoService;
  private final CursoService cursoService;
  private final UsuarioService usuarioService;

  @Autowired
  public VideoSecurityController(
      VideoService videoService, CursoService cursoService, UsuarioService usuarioService) {
    this.videoService = videoService;
    this.cursoService = cursoService;
    this.usuarioService = usuarioService;
  }

  @GetMapping("/cursos/{cursoId}/videos/{id}/secure")
  public String verificarAccesoVideo(
      @PathVariable Long cursoId,
      @PathVariable Long id,
      @RequestParam(value = "pdfId", required = false) Long pdfId,
      @RequestParam(value = "quizId", required = false) Long quizId,
      Model model,
      RedirectAttributes redirectAttributes) {
    Optional<Curso> cursoOpt = cursoService.findById(cursoId);
    Optional<Video> videoOpt = videoService.findById(id);

    if (cursoOpt.isPresent() && videoOpt.isPresent()) {
      // Verificar si el usuario está autenticado y tiene acceso al video
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
        String username = auth.getName();

        boolean isAdmin =
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        try {
          // Usar el método seguro para verificar inscripción que evita
          // ConcurrentModificationException
          boolean inscrito =
              isAdmin || usuarioService.verificarInscripcionSegura(username, cursoId);

          if (!inscrito) {
            redirectAttributes.addFlashAttribute(
                "error",
                "Necesitas estar inscrito en este curso para ver sus videos. Por favor, inscríbete"
                    + " primero.");
            return "redirect:/cursos/" + cursoId;
          }

          // Si el usuario está inscrito, redirigir al controlador principal de videos
          String redirect = "redirect:/cursos/" + cursoId + "/videos/" + id + "/view";
          String params = "";
          if (pdfId != null) {
            params = params + (params.isEmpty() ? "?" : "&") + "pdfId=" + pdfId;
          }
          if (quizId != null) {
            params = params + (params.isEmpty() ? "?" : "&") + "quizId=" + quizId;
          }
          return redirect + params;
        } catch (Exception e) {
          // En caso de error, redirigir al usuario a la página del curso con un mensaje
          redirectAttributes.addFlashAttribute(
              "error",
              "Ocurrió un error al verificar tu inscripción. Por favor, intenta de nuevo más"
                  + " tarde.");
          return "redirect:/cursos/" + cursoId;
        }
      }

      redirectAttributes.addFlashAttribute(
          "error", "Necesitas iniciar sesión e inscribirte en este curso para ver sus videos.");
      return "redirect:/login";
    }

    return "redirect:/cursos/" + cursoId + "/videos";
  }
}
