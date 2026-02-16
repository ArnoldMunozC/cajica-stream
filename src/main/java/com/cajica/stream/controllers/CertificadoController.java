package com.cajica.stream.controllers;

import com.cajica.stream.entities.Certificado;
import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.Usuario;
import com.cajica.stream.services.CertificadoPdfService;
import com.cajica.stream.services.CertificadoService;
import com.cajica.stream.services.CertificadoService.ElegibilidadCertificado;
import com.cajica.stream.services.CursoService;
import com.cajica.stream.services.UsuarioService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/certificados")
public class CertificadoController {

  private static final Logger logger = LoggerFactory.getLogger(CertificadoController.class);

  @Autowired private CertificadoService certificadoService;

  @Autowired private CertificadoPdfService certificadoPdfService;

  @Autowired private CursoService cursoService;

  @Autowired private UsuarioService usuarioService;

  @GetMapping("/curso/{cursoId}/estado")
  public String verEstadoCertificacion(@PathVariable("cursoId") Long cursoId, Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Optional<Usuario> usuarioOpt = usuarioService.findByUsername(auth.getName());
    Optional<Curso> cursoOpt = cursoService.findById(cursoId);

    if (usuarioOpt.isEmpty() || cursoOpt.isEmpty()) {
      return "redirect:/cursos";
    }

    Usuario usuario = usuarioOpt.get();
    Curso curso = cursoOpt.get();

    // Verificar si ya tiene certificado
    Optional<Certificado> certificadoExistente =
        certificadoService.findByUsuarioAndCurso(usuario.getId(), cursoId);

    if (certificadoExistente.isPresent()) {
      model.addAttribute("certificado", certificadoExistente.get());
      model.addAttribute("tieneCertificado", true);
    } else {
      ElegibilidadCertificado elegibilidad =
          certificadoService.verificarElegibilidad(usuario, curso);
      model.addAttribute("elegibilidad", elegibilidad);
      model.addAttribute("tieneCertificado", false);
    }

    model.addAttribute("curso", curso);
    return "certificados/estado";
  }

  @PostMapping("/curso/{cursoId}/generar")
  public String generarCertificado(
      @PathVariable("cursoId") Long cursoId, RedirectAttributes redirectAttributes) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Optional<Usuario> usuarioOpt = usuarioService.findByUsername(auth.getName());
    Optional<Curso> cursoOpt = cursoService.findById(cursoId);

    if (usuarioOpt.isEmpty() || cursoOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Curso o usuario no encontrado");
      return "redirect:/cursos";
    }

    Usuario usuario = usuarioOpt.get();
    Curso curso = cursoOpt.get();

    Certificado certificado = certificadoService.generarCertificado(usuario, curso);

    if (certificado != null) {
      redirectAttributes.addFlashAttribute("mensaje", "¡Certificado generado exitosamente!");
    } else {
      redirectAttributes.addFlashAttribute(
          "error", "No cumples con los requisitos para obtener el certificado");
    }

    return "redirect:/certificados/curso/" + cursoId + "/estado";
  }

  @GetMapping("/curso/{cursoId}/descargar")
  @ResponseBody
  public ResponseEntity<byte[]> descargarCertificado(@PathVariable("cursoId") Long cursoId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    logger.info("Descarga de certificado solicitada. username={}, cursoId={}", auth.getName(), cursoId);
    Optional<Usuario> usuarioOpt = usuarioService.findByUsername(auth.getName());

    if (usuarioOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Optional<Certificado> certificadoOpt =
        certificadoService.findByUsuarioAndCurso(usuarioOpt.get().getId(), cursoId);

    if (certificadoOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    try {
      byte[] pdfBytes = certificadoPdfService.generarCertificadoPdf(certificadoOpt.get());

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDispositionFormData(
          "attachment",
          "certificado_" + certificadoOpt.get().getCodigoVerificacion() + ".pdf");

      return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    } catch (Exception e) {
      logger.error(
          "Error generando/descargando certificado. username={}, cursoId={}, codigo={}",
          auth.getName(),
          cursoId,
          certificadoOpt.get().getCodigoVerificacion(),
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/verificar/{codigo}")
  public String verificarCertificado(@PathVariable("codigo") String codigo, Model model) {
    Optional<Certificado> certificadoOpt = certificadoService.findByCodigoVerificacion(codigo);

    if (certificadoOpt.isPresent()) {
      model.addAttribute("certificado", certificadoOpt.get());
      model.addAttribute("valido", true);
    } else {
      model.addAttribute("valido", false);
      model.addAttribute("codigo", codigo);
    }

    return "certificados/verificar";
  }

  @GetMapping("/mis-certificados")
  public String misCertificados(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Optional<Usuario> usuarioOpt = usuarioService.findByUsername(auth.getName());

    if (usuarioOpt.isEmpty()) {
      return "redirect:/login";
    }

    model.addAttribute("certificados", certificadoService.findByUsuario(usuarioOpt.get().getId()));
    return "certificados/mis-certificados";
  }

  @GetMapping("/debug/usuario/{usuarioId}/curso/{cursoId}")
  @ResponseBody
  public ResponseEntity<String> debugElegibilidad(@PathVariable("usuarioId") Long usuarioId, @PathVariable("cursoId") Long cursoId) {
    Optional<Usuario> usuarioOpt = usuarioService.findById(usuarioId);
    Optional<Curso> cursoOpt = cursoService.findById(cursoId);

    if (usuarioOpt.isEmpty() || cursoOpt.isEmpty()) {
      return ResponseEntity.ok("Usuario o curso no encontrado");
    }

    Usuario usuario = usuarioOpt.get();
    Curso curso = cursoOpt.get();

    ElegibilidadCertificado elegibilidad = certificadoService.verificarElegibilidad(usuario, curso);

    StringBuilder sb = new StringBuilder();
    sb.append("=== DEBUG ELEGIBILIDAD CERTIFICADO ===\n");
    sb.append("Usuario: ").append(usuario.getUsername()).append(" (ID: ").append(usuario.getId()).append(")\n");
    sb.append("Curso: ").append(curso.getNombre()).append(" (ID: ").append(curso.getId()).append(")\n\n");

    sb.append("VIDEOS:\n");
    sb.append("  - Total videos: ").append(elegibilidad.getTotalVideos()).append("\n");
    sb.append("  - Videos completados: ").append(elegibilidad.getVideosCompletados()).append("\n");
    sb.append("  - Videos completos: ").append(elegibilidad.isVideosCompletos()).append("\n\n");

    sb.append("QUIZZES:\n");
    sb.append("  - Total quizzes: ").append(elegibilidad.getTotalQuizzes()).append("\n");
    sb.append("  - Quizzes aprobados: ").append(elegibilidad.getQuizzesAprobados()).append("\n");
    sb.append("  - Quizzes completos: ").append(elegibilidad.isQuizzesCompletos()).append("\n");
    sb.append("  - Nota promedio: ").append(String.format("%.2f%%", elegibilidad.getNotaPromedio())).append("\n\n");

    sb.append("ELEGIBLE: ").append(elegibilidad.isElegible() ? "SÍ ✓" : "NO ✗").append("\n");

    boolean tieneCertificado = certificadoService.yaTieneCertificado(usuarioId, cursoId);
    sb.append("YA TIENE CERTIFICADO: ").append(tieneCertificado ? "SÍ" : "NO").append("\n\n");

    sb.append("=== ANÁLISIS DETALLADO ===\n");
    sb.append("Si NO es elegible, revisa:\n");
    sb.append("1. Que todos los videos estén marcados como completados (95% del video visto)\n");
    sb.append("2. Que todos los quizzes estén aprobados con nota >= 70%\n");
    sb.append("3. Si el certificado no se genera automáticamente, intenta acceder a:\n");
    sb.append("   /certificados/curso/").append(cursoId).append("/estado\n");
    sb.append("   y haz clic en 'Generar Certificado' manualmente.\n");

    return ResponseEntity.ok(sb.toString());
  }
}
