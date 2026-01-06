package com.cajica.stream.controllers;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.MaterialPdf;
import com.cajica.stream.entities.Quiz;
import com.cajica.stream.entities.QuizIntento;
import com.cajica.stream.entities.QuizPregunta;
import com.cajica.stream.entities.QuizRespuesta;
import com.cajica.stream.entities.Video;
import com.cajica.stream.services.CursoService;
import com.cajica.stream.services.MaterialPdfService;
import com.cajica.stream.services.QuizService;
import com.cajica.stream.services.UsuarioService;
import com.cajica.stream.services.VideoService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cursos/{cursoId}/videos")
public class VideoViewController {

  public static class ContenidoSeccionItem {
    private final String tipo; // VIDEO | PDF | QUIZ
    private final Integer orden;
    private final Video video;
    private final MaterialPdf pdf;
    private final Quiz quiz;

    private ContenidoSeccionItem(
        String tipo, Integer orden, Video video, MaterialPdf pdf, Quiz quiz) {
      this.tipo = tipo;
      this.orden = orden;
      this.video = video;
      this.pdf = pdf;
      this.quiz = quiz;
    }

    public static ContenidoSeccionItem fromVideo(Video video) {
      return new ContenidoSeccionItem("VIDEO", video.getOrden(), video, null, null);
    }

    public static ContenidoSeccionItem fromPdf(MaterialPdf pdf) {
      return new ContenidoSeccionItem("PDF", pdf.getOrden(), null, pdf, null);
    }

    public static ContenidoSeccionItem fromQuiz(Quiz quiz) {
      return new ContenidoSeccionItem("QUIZ", Integer.MAX_VALUE, null, null, quiz);
    }

    public String getTipo() {
      return tipo;
    }

    public Integer getOrden() {
      return orden;
    }

    public Video getVideo() {
      return video;
    }

    public MaterialPdf getPdf() {
      return pdf;
    }

    public Quiz getQuiz() {
      return quiz;
    }
  }

  private final VideoService videoService;
  private final CursoService cursoService;
  private final MaterialPdfService materialPdfService;
  private final QuizService quizService;
  private final UsuarioService usuarioService;

  @Autowired
  public VideoViewController(
      VideoService videoService,
      CursoService cursoService,
      MaterialPdfService materialPdfService,
      QuizService quizService,
      UsuarioService usuarioService) {
    this.videoService = videoService;
    this.cursoService = cursoService;
    this.materialPdfService = materialPdfService;
    this.quizService = quizService;
    this.usuarioService = usuarioService;
  }

  @PostMapping("/{id}/quiz/{quizId}/submit")
  public String submitQuiz(
      @PathVariable Long cursoId,
      @PathVariable Long id,
      @PathVariable Long quizId,
      @RequestParam MultiValueMap<String, String> params,
      RedirectAttributes redirectAttributes) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
      return "redirect:/login";
    }

    boolean isAdmin =
        auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    String username = auth.getName();

    try {
      if (!isAdmin) {
        boolean inscrito = usuarioService.verificarInscripcionSegura(username, cursoId);
        if (!inscrito) {
          redirectAttributes.addFlashAttribute(
              "error", "Necesitas estar inscrito en este curso para resolver el quiz");
          return "redirect:/cursos/" + cursoId;
        }
      }

      Optional<Quiz> quizOpt = quizService.findById(quizId);
      if (quizOpt.isEmpty()
          || quizOpt.get().getCurso() == null
          || !quizOpt.get().getCurso().getId().equals(cursoId)) {
        redirectAttributes.addFlashAttribute("error", "Quiz no encontrado");
        return "redirect:/cursos/" + cursoId + "/videos/" + id;
      }

      Quiz quiz = quizOpt.get();
      if (!quiz.isActivo()) {
        redirectAttributes.addFlashAttribute("error", "Este quiz está deshabilitado");
        return "redirect:/cursos/" + cursoId + "/videos/" + id;
      }

      HashMap<Long, List<Long>> respuestas = new HashMap<>();
      for (QuizPregunta p : quiz.getPreguntas()) {
        String key = "q_" + p.getId();
        List<String> values = params.get(key);
        if (values == null) {
          continue;
        }
        List<Long> ids = new ArrayList<>();
        for (String v : values) {
          try {
            ids.add(Long.parseLong(v));
          } catch (NumberFormatException ignored) {
          }
        }
        respuestas.put(p.getId(), ids);
      }

      QuizIntento intento = quizService.submitAttempt(quizId, username, respuestas);
      int porcentaje =
          intento.getTotalPreguntas() == 0
              ? 0
              : (int) Math.round((intento.getPuntaje() * 100.0) / intento.getTotalPreguntas());

      redirectAttributes.addFlashAttribute(
          "mensaje",
          "Resultado: "
              + porcentaje
              + "% ("
              + intento.getPuntaje()
              + "/"
              + intento.getTotalPreguntas()
              + ")");
      return "redirect:/cursos/" + cursoId + "/videos/" + id + "/view?quizId=" + quizId;
    } catch (IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/cursos/" + cursoId + "/videos/" + id + "/view?quizId=" + quizId;
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "No se pudo enviar el quiz");
      return "redirect:/cursos/" + cursoId + "/videos/" + id + "/view?quizId=" + quizId;
    }
  }

  @GetMapping
  public String listarVideos(@PathVariable Long cursoId, Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    if (curso.isPresent()) {
      List<Video> videos = videoService.findByCursoId(cursoId);
      model.addAttribute("curso", curso.get());
      model.addAttribute("videos", videos);
      return "videos/lista";
    } else {
      return "redirect:/cursos";
    }
  }

  @GetMapping("/{id}")
  public String verVideo(@PathVariable Long cursoId, @PathVariable Long id, Model model) {
    // Redirigir primero al controlador de seguridad para verificar acceso
    return "redirect:/cursos/" + cursoId + "/videos/" + id + "/secure";
  }

  @GetMapping("/{id}/view")
  public String mostrarVideo(
      @PathVariable Long cursoId,
      @PathVariable Long id,
      @RequestParam(value = "pdfId", required = false) Long pdfId,
      @RequestParam(value = "quizId", required = false) Long quizId,
      Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    Optional<Video> video = videoService.findById(id);

    if (curso.isPresent() && video.isPresent()) {
      model.addAttribute("curso", curso.get());
      model.addAttribute("video", video.get());

      // Obtener videos relacionados del mismo curso
      List<Video> videosRelacionados = videoService.findByCursoId(cursoId);
      model.addAttribute("videosRelacionados", videosRelacionados);

      LinkedHashMap<String, List<Video>> videosPorSeccion =
          videosRelacionados.stream()
              .collect(
                  Collectors.groupingBy(
                      v ->
                          (v.getSeccion() == null || v.getSeccion().isBlank())
                              ? "Sin sección"
                              : v.getSeccion(),
                      LinkedHashMap::new,
                      Collectors.toList()));

      List<MaterialPdf> pdfs = materialPdfService.findByCursoId(cursoId);
      LinkedHashMap<String, List<MaterialPdf>> pdfsPorSeccion =
          pdfs.stream()
              .collect(
                  Collectors.groupingBy(
                      p ->
                          (p.getSeccion() == null || p.getSeccion().isBlank())
                              ? "Sin sección"
                              : p.getSeccion(),
                      LinkedHashMap::new,
                      Collectors.toList()));

      Set<String> secciones = new LinkedHashSet<>();
      secciones.addAll(videosPorSeccion.keySet());
      secciones.addAll(pdfsPorSeccion.keySet());

      List<Quiz> quizzes = quizService.findByCursoId(cursoId);
      LinkedHashMap<String, Quiz> quizPorSeccion = new LinkedHashMap<>();
      for (Quiz q : quizzes) {
        String sec =
            (q.getSeccion() == null || q.getSeccion().isBlank()) ? "Sin sección" : q.getSeccion();
        if (!quizPorSeccion.containsKey(sec)) {
          quizPorSeccion.put(sec, q);
        }
      }
      secciones.addAll(quizPorSeccion.keySet());

      Comparator<ContenidoSeccionItem> comparator =
          Comparator.comparing(
                  (ContenidoSeccionItem i) ->
                      i.getOrden() == null ? Integer.MAX_VALUE : i.getOrden())
              .thenComparing(
                  i -> "VIDEO".equals(i.getTipo()) ? 0 : ("PDF".equals(i.getTipo()) ? 1 : 2))
              .thenComparing(
                  i -> {
                    if (i.getVideo() != null) {
                      return i.getVideo().getId();
                    }
                    if (i.getPdf() != null) {
                      return i.getPdf().getId();
                    }
                    if (i.getQuiz() != null) {
                      return i.getQuiz().getId();
                    }
                    return Long.MAX_VALUE;
                  });

      LinkedHashMap<String, List<ContenidoSeccionItem>> contenidoPorSeccion = new LinkedHashMap<>();
      for (String seccion : secciones) {
        List<ContenidoSeccionItem> items = new ArrayList<>();
        List<Video> vs = videosPorSeccion.get(seccion);
        if (vs != null) {
          for (Video v : vs) {
            items.add(ContenidoSeccionItem.fromVideo(v));
          }
        }
        List<MaterialPdf> ps = pdfsPorSeccion.get(seccion);
        if (ps != null) {
          for (MaterialPdf p : ps) {
            items.add(ContenidoSeccionItem.fromPdf(p));
          }
        }

        Quiz quiz = quizPorSeccion.get(seccion);
        if (quiz != null) {
          items.add(ContenidoSeccionItem.fromQuiz(quiz));
        }
        items.sort(comparator);
        contenidoPorSeccion.put(seccion, items);
      }

      model.addAttribute("contenidoPorSeccion", contenidoPorSeccion);

      MaterialPdf pdfSeleccionado = null;
      if (pdfId != null) {
        Optional<MaterialPdf> pdfOpt = materialPdfService.findById(pdfId);
        if (pdfOpt.isPresent()
            && pdfOpt.get().getCurso() != null
            && pdfOpt.get().getCurso().getId().equals(cursoId)) {
          pdfSeleccionado = pdfOpt.get();
        }
      }
      model.addAttribute("pdfSeleccionado", pdfSeleccionado);

      Quiz quizSeleccionado = null;
      if (quizId != null) {
        Optional<Quiz> quizOpt = quizService.findById(quizId);
        if (quizOpt.isPresent()
            && quizOpt.get().getCurso() != null
            && quizOpt.get().getCurso().getId().equals(cursoId)) {
          quizSeleccionado = quizOpt.get();
        }
      }
      model.addAttribute("quizSeleccionado", quizSeleccionado);

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      boolean isLogged =
          auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
      boolean isAdmin =
          isLogged
              && auth.getAuthorities().stream()
                  .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

      if (quizSeleccionado != null && isLogged) {
        final Quiz quizSel = quizSeleccionado;
        usuarioService
            .findByUsername(auth.getName())
            .ifPresent(
                u -> {
                  Optional<QuizIntento> intentoOpt =
                      quizService.findUltimoIntento(quizSel.getId(), u.getId());
                  if (intentoOpt.isPresent()) {
                    QuizIntento intento = intentoOpt.get();
                    List<QuizRespuesta> respuestas =
                        quizService.findRespuestasByIntentoId(intento.getId());
                    Map<Long, Set<Long>> seleccionadasPorPregunta =
                        respuestas.stream()
                            .filter(r -> r.getOpcion() != null)
                            .collect(
                                Collectors.groupingBy(
                                    r -> r.getPregunta().getId(),
                                    Collectors.mapping(
                                        r -> r.getOpcion().getId(), Collectors.toSet())));

                    Map<Long, Boolean> preguntaCorrecta = new HashMap<>();
                    for (QuizPregunta p : quizSel.getPreguntas()) {
                      Set<Long> seleccion =
                          seleccionadasPorPregunta.getOrDefault(
                              p.getId(), java.util.Collections.emptySet());
                      Set<Long> correctas =
                          p.getOpciones().stream()
                              .filter(o -> o.isCorrecta())
                              .map(o -> o.getId())
                              .collect(Collectors.toSet());
                      boolean ok;
                      if (p.getTipo() == com.cajica.stream.entities.QuizPreguntaTipo.UNICA) {
                        ok = seleccion.size() == 1 && seleccion.equals(correctas);
                      } else {
                        ok = seleccion.equals(correctas);
                      }
                      preguntaCorrecta.put(p.getId(), ok);
                    }

                    model.addAttribute("quizIntento", intento);
                    model.addAttribute("quizRespuestas", respuestas);
                    model.addAttribute("quizSeleccionadasPorPregunta", seleccionadasPorPregunta);
                    model.addAttribute("quizPreguntaCorrecta", preguntaCorrecta);
                  }
                });
      }

      String seccionActual;
      if (quizSeleccionado != null) {
        seccionActual =
            (quizSeleccionado.getSeccion() == null || quizSeleccionado.getSeccion().isBlank())
                ? "Sin sección"
                : quizSeleccionado.getSeccion();
      } else if (pdfSeleccionado != null) {
        seccionActual =
            (pdfSeleccionado.getSeccion() == null || pdfSeleccionado.getSeccion().isBlank())
                ? "Sin sección"
                : pdfSeleccionado.getSeccion();
      } else {
        seccionActual =
            (video.get().getSeccion() == null || video.get().getSeccion().isBlank())
                ? "Sin sección"
                : video.get().getSeccion();
      }
      model.addAttribute("seccionActual", seccionActual);

      return "videos/reproducir";
    } else {
      return "redirect:/cursos/" + cursoId;
    }
  }

  @GetMapping("/nuevo")
  @PreAuthorize("hasRole('ADMIN')")
  public String mostrarFormularioNuevo(@PathVariable Long cursoId, Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    if (curso.isPresent()) {
      model.addAttribute("curso", curso.get());
      model.addAttribute("video", new Video());
      model.addAttribute("accion", "crear");
      return "videos/formulario";
    } else {
      return "redirect:/cursos";
    }
  }

  @PostMapping("/nuevo")
  @PreAuthorize("hasRole('ADMIN')")
  public String crearVideo(
      @PathVariable Long cursoId,
      @ModelAttribute Video video,
      @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
      @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
      RedirectAttributes redirectAttributes) {

    // Validar que al menos uno de los dos campos esté presente: videoUrl o videoFile
    if ((video.getVideoUrl() == null || video.getVideoUrl().isEmpty())
        && (videoFile == null || videoFile.isEmpty())) {
      redirectAttributes.addFlashAttribute(
          "error", "Debes proporcionar una URL de video o subir un archivo de video");
      return "redirect:/cursos/" + cursoId + "/videos/nuevo";
    }

    Video nuevoVideo = videoService.addVideoToCurso(cursoId, video, thumbnail, videoFile);

    if (nuevoVideo != null) {
      redirectAttributes.addFlashAttribute("mensaje", "Video creado exitosamente");
      return "redirect:/cursos/" + cursoId + "/videos/" + nuevoVideo.getId();
    } else {
      return "redirect:/cursos/" + cursoId;
    }
  }

  @GetMapping("/{id}/editar")
  @PreAuthorize("hasRole('ADMIN')")
  public String mostrarFormularioEditar(
      @PathVariable Long cursoId, @PathVariable Long id, Model model) {

    Optional<Curso> curso = cursoService.findById(cursoId);
    Optional<Video> video = videoService.findById(id);

    if (curso.isPresent() && video.isPresent()) {
      model.addAttribute("curso", curso.get());
      model.addAttribute("video", video.get());
      model.addAttribute("accion", "editar");
      return "videos/formulario";
    } else {
      return "redirect:/cursos/" + cursoId;
    }
  }

  @PostMapping("/{id}/editar")
  @PreAuthorize("hasRole('ADMIN')")
  public String actualizarVideo(
      @PathVariable Long cursoId,
      @PathVariable Long id,
      @ModelAttribute Video video,
      @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
      @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
      RedirectAttributes redirectAttributes) {

    // Validar que al menos uno de los dos campos esté presente: videoUrl o videoFile
    // Si no hay videoUrl y no hay un archivo nuevo, verificar si ya existe un videoFilePath
    Optional<Video> existingVideo = videoService.findById(id);
    boolean hasExistingVideoFile =
        existingVideo.isPresent()
            && existingVideo.get().getVideoFilePath() != null
            && !existingVideo.get().getVideoFilePath().isEmpty();

    if ((video.getVideoUrl() == null || video.getVideoUrl().isEmpty())
        && (videoFile == null || videoFile.isEmpty())
        && !hasExistingVideoFile) {
      redirectAttributes.addFlashAttribute(
          "error", "Debes proporcionar una URL de video o subir un archivo de video");
      return "redirect:/cursos/" + cursoId + "/videos/" + id + "/editar";
    }

    Video videoActualizado = videoService.update(id, video, thumbnail, videoFile);

    if (videoActualizado != null) {
      redirectAttributes.addFlashAttribute("mensaje", "Video actualizado exitosamente");
      return "redirect:/cursos/" + cursoId + "/videos/" + videoActualizado.getId();
    } else {
      return "redirect:/cursos/" + cursoId;
    }
  }

  @GetMapping("/{id}/eliminar")
  @PreAuthorize("hasRole('ADMIN')")
  public String eliminarVideo(
      @PathVariable Long cursoId, @PathVariable Long id, RedirectAttributes redirectAttributes) {

    Optional<Video> video = videoService.findById(id);
    if (video.isPresent()) {
      videoService.deleteById(id);
      redirectAttributes.addFlashAttribute("mensaje", "Video eliminado exitosamente");
    }

    return "redirect:/cursos/" + cursoId + "/videos";
  }
}
