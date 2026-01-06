package com.cajica.stream.controllers;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.Quiz;
import com.cajica.stream.entities.QuizOpcion;
import com.cajica.stream.entities.QuizPregunta;
import com.cajica.stream.entities.QuizPreguntaTipo;
import com.cajica.stream.repositories.QuizOpcionRepository;
import com.cajica.stream.repositories.QuizPreguntaRepository;
import com.cajica.stream.repositories.QuizRepository;
import com.cajica.stream.services.CursoService;
import java.util.List;
import java.util.Optional;
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
@RequestMapping("/cursos/{cursoId}/quizzes")
@PreAuthorize("hasRole('ADMIN')")
public class QuizAdminController {

  private final CursoService cursoService;
  private final QuizRepository quizRepository;
  private final QuizPreguntaRepository quizPreguntaRepository;
  private final QuizOpcionRepository quizOpcionRepository;

  public QuizAdminController(
      CursoService cursoService,
      QuizRepository quizRepository,
      QuizPreguntaRepository quizPreguntaRepository,
      QuizOpcionRepository quizOpcionRepository) {
    this.cursoService = cursoService;
    this.quizRepository = quizRepository;
    this.quizPreguntaRepository = quizPreguntaRepository;
    this.quizOpcionRepository = quizOpcionRepository;
  }

  @GetMapping("/nuevo")
  public String mostrarFormularioNuevo(@PathVariable Long cursoId, Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    if (curso.isEmpty()) {
      return "redirect:/cursos";
    }

    model.addAttribute("curso", curso.get());
    model.addAttribute("quiz", new Quiz());
    return "quizzes/formulario";
  }

  @PostMapping("/nuevo")
  public String crearQuiz(
      @PathVariable Long cursoId,
      @RequestParam("titulo") String titulo,
      @RequestParam("seccion") String seccion,
      @RequestParam("maxIntentos") Integer maxIntentos,
      @RequestParam(value = "activo", defaultValue = "false") boolean activo,
      RedirectAttributes redirectAttributes) {

    Optional<Curso> cursoOpt = cursoService.findById(cursoId);
    if (cursoOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Curso no encontrado");
      return "redirect:/cursos";
    }

    String seccionNormalizada = (seccion == null || seccion.isBlank()) ? null : seccion.trim();
    if (seccionNormalizada == null) {
      redirectAttributes.addFlashAttribute("error", "La sección es obligatoria para el quiz");
      return "redirect:/cursos/" + cursoId + "/quizzes/nuevo";
    }

    Optional<Quiz> existente = quizRepository.findByCursoIdAndSeccion(cursoId, seccionNormalizada);
    if (existente.isPresent()) {
      redirectAttributes.addFlashAttribute(
          "error", "Ya existe un quiz para la sección '" + seccionNormalizada + "'");
      return "redirect:/cursos/" + cursoId + "/quizzes/nuevo";
    }

    Quiz quiz = new Quiz();
    quiz.setTitulo(titulo);
    quiz.setSeccion(seccionNormalizada);
    quiz.setMaxIntentos(maxIntentos == null || maxIntentos < 1 ? 1 : maxIntentos);
    quiz.setActivo(activo);
    quiz.setCurso(cursoOpt.get());

    Quiz saved = quizRepository.save(quiz);
    redirectAttributes.addFlashAttribute("mensaje", "Quiz creado exitosamente");
    return "redirect:/cursos/" + cursoId + "/quizzes/" + saved.getId() + "/preguntas";
  }

  @GetMapping("/{quizId}/editar")
  public String mostrarFormularioEditar(
      @PathVariable Long cursoId, @PathVariable Long quizId, Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    Optional<Quiz> quiz = quizRepository.findById(quizId);
    if (curso.isEmpty() || quiz.isEmpty()) {
      return "redirect:/cursos/" + cursoId + "/videos";
    }

    model.addAttribute("curso", curso.get());
    model.addAttribute("quiz", quiz.get());
    return "quizzes/formulario";
  }

  @PostMapping("/{quizId}/editar")
  public String editarQuiz(
      @PathVariable Long cursoId,
      @PathVariable Long quizId,
      @RequestParam("titulo") String titulo,
      @RequestParam("seccion") String seccion,
      @RequestParam("maxIntentos") Integer maxIntentos,
      @RequestParam(value = "activo", defaultValue = "false") boolean activo,
      RedirectAttributes redirectAttributes) {

    Optional<Quiz> quizOpt = quizRepository.findById(quizId);
    if (quizOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Quiz no encontrado");
      return "redirect:/cursos/" + cursoId + "/videos";
    }

    Quiz quiz = quizOpt.get();

    String seccionNormalizada = (seccion == null || seccion.isBlank()) ? null : seccion.trim();
    if (seccionNormalizada == null) {
      redirectAttributes.addFlashAttribute("error", "La sección es obligatoria para el quiz");
      return "redirect:/cursos/" + cursoId + "/quizzes/" + quizId + "/editar";
    }

    Optional<Quiz> existente = quizRepository.findByCursoIdAndSeccion(cursoId, seccionNormalizada);
    if (existente.isPresent() && !existente.get().getId().equals(quizId)) {
      redirectAttributes.addFlashAttribute(
          "error", "Ya existe un quiz para la sección '" + seccionNormalizada + "'");
      return "redirect:/cursos/" + cursoId + "/quizzes/" + quizId + "/editar";
    }

    quiz.setTitulo(titulo);
    quiz.setSeccion(seccionNormalizada);
    quiz.setMaxIntentos(maxIntentos == null || maxIntentos < 1 ? 1 : maxIntentos);
    quiz.setActivo(activo);

    quizRepository.save(quiz);
    redirectAttributes.addFlashAttribute("mensaje", "Quiz actualizado");
    return "redirect:/cursos/" + cursoId + "/quizzes/" + quizId + "/preguntas";
  }

  @GetMapping("/{quizId}/preguntas")
  public String listarPreguntas(
      @PathVariable Long cursoId, @PathVariable Long quizId, Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    Optional<Quiz> quiz = quizRepository.findById(quizId);
    if (curso.isEmpty() || quiz.isEmpty()) {
      return "redirect:/cursos/" + cursoId + "/videos";
    }

    List<QuizPregunta> preguntas = quizPreguntaRepository.findByQuizIdOrderByOrdenAscIdAsc(quizId);

    model.addAttribute("curso", curso.get());
    model.addAttribute("quiz", quiz.get());
    model.addAttribute("preguntas", preguntas);
    model.addAttribute("tipos", QuizPreguntaTipo.values());
    return "quizzes/preguntas";
  }

  @GetMapping("/{quizId}/preguntas/nueva")
  public String mostrarFormularioPreguntaNueva(
      @PathVariable Long cursoId, @PathVariable Long quizId, Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    Optional<Quiz> quiz = quizRepository.findById(quizId);
    if (curso.isEmpty() || quiz.isEmpty()) {
      return "redirect:/cursos/" + cursoId + "/videos";
    }

    QuizPregunta pregunta = new QuizPregunta();

    model.addAttribute("curso", curso.get());
    model.addAttribute("quiz", quiz.get());
    model.addAttribute("pregunta", pregunta);
    model.addAttribute("tipos", QuizPreguntaTipo.values());
    return "quizzes/pregunta_formulario";
  }

  @PostMapping("/{quizId}/preguntas/nueva")
  public String crearPregunta(
      @PathVariable Long cursoId,
      @PathVariable Long quizId,
      @RequestParam("enunciado") String enunciado,
      @RequestParam("tipo") QuizPreguntaTipo tipo,
      @RequestParam(value = "orden", required = false) Integer orden,
      RedirectAttributes redirectAttributes) {

    Optional<Quiz> quizOpt = quizRepository.findById(quizId);
    if (quizOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Quiz no encontrado");
      return "redirect:/cursos/" + cursoId + "/videos";
    }

    if (enunciado == null || enunciado.isBlank()) {
      redirectAttributes.addFlashAttribute("error", "El enunciado es obligatorio");
      return "redirect:/cursos/" + cursoId + "/quizzes/" + quizId + "/preguntas/nueva";
    }

    QuizPregunta pregunta = new QuizPregunta();
    pregunta.setQuiz(quizOpt.get());
    pregunta.setEnunciado(enunciado);
    pregunta.setTipo(tipo);
    pregunta.setOrden(orden);

    QuizPregunta saved = quizPreguntaRepository.save(pregunta);
    redirectAttributes.addFlashAttribute("mensaje", "Pregunta creada. Ahora agrega opciones.");
    return "redirect:/cursos/"
        + cursoId
        + "/quizzes/"
        + quizId
        + "/preguntas/"
        + saved.getId()
        + "/opciones";
  }

  @GetMapping("/{quizId}/preguntas/{preguntaId}/editar")
  public String mostrarFormularioPreguntaEditar(
      @PathVariable Long cursoId,
      @PathVariable Long quizId,
      @PathVariable Long preguntaId,
      Model model) {

    Optional<Curso> curso = cursoService.findById(cursoId);
    Optional<Quiz> quiz = quizRepository.findById(quizId);
    Optional<QuizPregunta> pregunta = quizPreguntaRepository.findById(preguntaId);
    if (curso.isEmpty() || quiz.isEmpty() || pregunta.isEmpty()) {
      return "redirect:/cursos/" + cursoId + "/quizzes/" + quizId + "/preguntas";
    }

    model.addAttribute("curso", curso.get());
    model.addAttribute("quiz", quiz.get());
    model.addAttribute("pregunta", pregunta.get());
    model.addAttribute("tipos", QuizPreguntaTipo.values());
    return "quizzes/pregunta_formulario";
  }

  @PostMapping("/{quizId}/preguntas/{preguntaId}/editar")
  public String editarPregunta(
      @PathVariable Long cursoId,
      @PathVariable Long quizId,
      @PathVariable Long preguntaId,
      @RequestParam("enunciado") String enunciado,
      @RequestParam("tipo") QuizPreguntaTipo tipo,
      @RequestParam(value = "orden", required = false) Integer orden,
      RedirectAttributes redirectAttributes) {

    Optional<QuizPregunta> preguntaOpt = quizPreguntaRepository.findById(preguntaId);
    if (preguntaOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Pregunta no encontrada");
      return "redirect:/cursos/" + cursoId + "/quizzes/" + quizId + "/preguntas";
    }

    if (enunciado == null || enunciado.isBlank()) {
      redirectAttributes.addFlashAttribute("error", "El enunciado es obligatorio");
      return "redirect:/cursos/"
          + cursoId
          + "/quizzes/"
          + quizId
          + "/preguntas/"
          + preguntaId
          + "/editar";
    }

    QuizPregunta pregunta = preguntaOpt.get();
    pregunta.setEnunciado(enunciado);
    pregunta.setTipo(tipo);
    pregunta.setOrden(orden);

    quizPreguntaRepository.save(pregunta);
    redirectAttributes.addFlashAttribute("mensaje", "Pregunta actualizada");
    return "redirect:/cursos/" + cursoId + "/quizzes/" + quizId + "/preguntas";
  }

  @GetMapping("/{quizId}/preguntas/{preguntaId}/eliminar")
  public String eliminarPregunta(
      @PathVariable Long cursoId,
      @PathVariable Long quizId,
      @PathVariable Long preguntaId,
      RedirectAttributes redirectAttributes) {

    quizPreguntaRepository.deleteById(preguntaId);
    redirectAttributes.addFlashAttribute("mensaje", "Pregunta eliminada");
    return "redirect:/cursos/" + cursoId + "/quizzes/" + quizId + "/preguntas";
  }

  @GetMapping("/{quizId}/preguntas/{preguntaId}/opciones")
  public String listarOpciones(
      @PathVariable Long cursoId,
      @PathVariable Long quizId,
      @PathVariable Long preguntaId,
      Model model) {

    Optional<Curso> curso = cursoService.findById(cursoId);
    Optional<Quiz> quiz = quizRepository.findById(quizId);
    Optional<QuizPregunta> pregunta = quizPreguntaRepository.findById(preguntaId);
    if (curso.isEmpty() || quiz.isEmpty() || pregunta.isEmpty()) {
      return "redirect:/cursos/" + cursoId + "/quizzes/" + quizId + "/preguntas";
    }

    List<QuizOpcion> opciones =
        quizOpcionRepository.findByPreguntaIdOrderByOrdenAscIdAsc(preguntaId);

    model.addAttribute("curso", curso.get());
    model.addAttribute("quiz", quiz.get());
    model.addAttribute("pregunta", pregunta.get());
    model.addAttribute("opciones", opciones);
    model.addAttribute("opcion", new QuizOpcion());
    return "quizzes/opciones";
  }

  @PostMapping("/{quizId}/preguntas/{preguntaId}/opciones/nueva")
  public String crearOpcion(
      @PathVariable Long cursoId,
      @PathVariable Long quizId,
      @PathVariable Long preguntaId,
      @RequestParam("texto") String texto,
      @RequestParam(value = "correcta", defaultValue = "false") boolean correcta,
      @RequestParam(value = "orden", required = false) Integer orden,
      RedirectAttributes redirectAttributes) {

    Optional<QuizPregunta> preguntaOpt = quizPreguntaRepository.findById(preguntaId);
    if (preguntaOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Pregunta no encontrada");
      return "redirect:/cursos/" + cursoId + "/quizzes/" + quizId + "/preguntas";
    }

    if (texto == null || texto.isBlank()) {
      redirectAttributes.addFlashAttribute("error", "El texto de la opción es obligatorio");
      return "redirect:/cursos/"
          + cursoId
          + "/quizzes/"
          + quizId
          + "/preguntas/"
          + preguntaId
          + "/opciones";
    }

    QuizPregunta pregunta = preguntaOpt.get();

    if (pregunta.getTipo() == QuizPreguntaTipo.UNICA && correcta) {
      List<QuizOpcion> existentes =
          quizOpcionRepository.findByPreguntaIdOrderByOrdenAscIdAsc(preguntaId);
      for (QuizOpcion o : existentes) {
        o.setCorrecta(false);
      }
      quizOpcionRepository.saveAll(existentes);
    }

    QuizOpcion opcion = new QuizOpcion();
    opcion.setPregunta(pregunta);
    opcion.setTexto(texto);
    opcion.setCorrecta(correcta);
    opcion.setOrden(orden);

    quizOpcionRepository.save(opcion);
    redirectAttributes.addFlashAttribute("mensaje", "Opción agregada");
    return "redirect:/cursos/"
        + cursoId
        + "/quizzes/"
        + quizId
        + "/preguntas/"
        + preguntaId
        + "/opciones";
  }

  @GetMapping("/{quizId}/preguntas/{preguntaId}/opciones/{opcionId}/eliminar")
  public String eliminarOpcion(
      @PathVariable Long cursoId,
      @PathVariable Long quizId,
      @PathVariable Long preguntaId,
      @PathVariable Long opcionId,
      RedirectAttributes redirectAttributes) {

    quizOpcionRepository.deleteById(opcionId);
    redirectAttributes.addFlashAttribute("mensaje", "Opción eliminada");
    return "redirect:/cursos/"
        + cursoId
        + "/quizzes/"
        + quizId
        + "/preguntas/"
        + preguntaId
        + "/opciones";
  }
}
