package com.cajica.stream.services;

import com.cajica.stream.entities.Quiz;
import com.cajica.stream.entities.QuizIntento;
import com.cajica.stream.entities.QuizOpcion;
import com.cajica.stream.entities.QuizPregunta;
import com.cajica.stream.entities.QuizPreguntaTipo;
import com.cajica.stream.entities.QuizRespuesta;
import com.cajica.stream.entities.Usuario;
import com.cajica.stream.repositories.QuizIntentoRepository;
import com.cajica.stream.repositories.QuizRepository;
import com.cajica.stream.repositories.QuizRespuestaRepository;
import com.cajica.stream.repositories.UsuarioRepository;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {

  private static final int UMBRAL_APROBACION_PORCENTAJE = 80;

  private final QuizRepository quizRepository;
  private final QuizIntentoRepository quizIntentoRepository;
  private final QuizRespuestaRepository quizRespuestaRepository;
  private final UsuarioRepository usuarioRepository;

  public QuizService(
      QuizRepository quizRepository,
      QuizIntentoRepository quizIntentoRepository,
      QuizRespuestaRepository quizRespuestaRepository,
      UsuarioRepository usuarioRepository) {
    this.quizRepository = quizRepository;
    this.quizIntentoRepository = quizIntentoRepository;
    this.quizRespuestaRepository = quizRespuestaRepository;
    this.usuarioRepository = usuarioRepository;
  }

  @Transactional(readOnly = true)
  public Optional<Quiz> findById(Long id) {
    Optional<Quiz> quizOpt = quizRepository.findById(id);
    quizOpt.ifPresent(
        q -> {
          q.getPreguntas().forEach(p -> p.getOpciones().size());
        });
    return quizOpt;
  }

  @Transactional(readOnly = true)
  public List<Quiz> findByCursoId(Long cursoId) {
    return quizRepository.findByCursoId(cursoId);
  }

  @Transactional
  public QuizIntento submitAttempt(Long quizId, String username, Map<Long, List<Long>> respuestas) {
    Usuario usuario =
        usuarioRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    Quiz quiz =
        quizRepository
            .findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("Cuestionario no encontrado"));

    quiz.getPreguntas().forEach(p -> p.getOpciones().size());

    Integer maxIntentos = quiz.getMaxIntentos() == null ? 1 : quiz.getMaxIntentos();

    Long intentosRealizados =
        quizIntentoRepository.countByQuizIdAndUsuarioId(quizId, usuario.getId());
    if (intentosRealizados != null && intentosRealizados.intValue() >= maxIntentos) {
      throw new IllegalStateException(
          "Has alcanzado el número máximo de intentos para este cuestionario");
    }

    int numeroIntento = intentosRealizados == null ? 1 : intentosRealizados.intValue() + 1;

    int totalPreguntas = quiz.getPreguntas().size();
    int correctas = 0;

    QuizIntento intento = new QuizIntento();
    intento.setQuiz(quiz);
    intento.setUsuario(usuario);
    intento.setNumeroIntento(numeroIntento);

    List<QuizRespuesta> respuestasEntities = new ArrayList<>();

    for (QuizPregunta pregunta : quiz.getPreguntas()) {
      List<Long> seleccionadas = respuestas.getOrDefault(pregunta.getId(), Collections.emptyList());
      Set<Long> seleccionSet = new HashSet<>(seleccionadas);

      Set<Long> correctSet = new HashSet<>();
      for (QuizOpcion o : pregunta.getOpciones()) {
        if (o.isCorrecta()) {
          correctSet.add(o.getId());
        }
      }

      boolean preguntaCorrecta;
      if (pregunta.getTipo() == QuizPreguntaTipo.UNICA) {
        preguntaCorrecta = seleccionSet.size() == 1 && seleccionSet.equals(correctSet);
      } else {
        preguntaCorrecta = seleccionSet.equals(correctSet);
      }

      if (preguntaCorrecta) {
        correctas++;
      }

      Map<Long, QuizOpcion> opcionesPorId = new HashMap<>();
      for (QuizOpcion o : pregunta.getOpciones()) {
        opcionesPorId.put(o.getId(), o);
      }

      for (Long opcionId : seleccionSet) {
        QuizOpcion opcion = opcionesPorId.get(opcionId);
        if (opcion == null) {
          continue;
        }
        QuizRespuesta r = new QuizRespuesta();
        r.setIntento(intento);
        r.setPregunta(pregunta);
        r.setOpcion(opcion);
        r.setCorrecta(opcion.isCorrecta());
        respuestasEntities.add(r);
      }

      if (seleccionSet.isEmpty()) {
        QuizRespuesta r = new QuizRespuesta();
        r.setIntento(intento);
        r.setPregunta(pregunta);
        r.setOpcion(null);
        r.setCorrecta(preguntaCorrecta);
        respuestasEntities.add(r);
      }
    }

    int porcentaje =
        totalPreguntas == 0 ? 0 : (int) Math.round((correctas * 100.0) / totalPreguntas);
    boolean aprobado = porcentaje >= UMBRAL_APROBACION_PORCENTAJE;

    intento.setPuntaje(correctas);
    intento.setTotalPreguntas(totalPreguntas);
    intento.setAprobado(aprobado);

    QuizIntento saved = quizIntentoRepository.save(intento);

    for (QuizRespuesta r : respuestasEntities) {
      r.setIntento(saved);
    }
    quizRespuestaRepository.saveAll(respuestasEntities);

    saved.setRespuestas(respuestasEntities);
    return saved;
  }

  @Transactional(readOnly = true)
  public List<QuizIntento> findIntentos(Long quizId, Long usuarioId) {
    return quizIntentoRepository.findByQuizIdAndUsuarioIdOrderByNumeroIntentoDesc(
        quizId, usuarioId);
  }

  @Transactional(readOnly = true)
  public Optional<QuizIntento> findUltimoIntento(Long quizId, Long usuarioId) {
    return quizIntentoRepository.findTopByQuizIdAndUsuarioIdOrderByNumeroIntentoDesc(
        quizId, usuarioId);
  }

  @Transactional(readOnly = true)
  public List<QuizRespuesta> findRespuestasByIntentoId(Long intentoId) {
    return quizRespuestaRepository.findByIntentoId(intentoId);
  }

  public int getUmbralAprobacionPorcentaje() {
    return UMBRAL_APROBACION_PORCENTAJE;
  }
}
