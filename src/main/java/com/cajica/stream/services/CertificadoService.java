package com.cajica.stream.services;

import com.cajica.stream.entities.*;
import com.cajica.stream.repositories.CertificadoRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CertificadoService {

  private static final double NOTA_MINIMA_APROBACION = 70.0;

  @Autowired private CertificadoRepository certificadoRepository;

  @Autowired private VideoProgresoService videoProgresoService;

  @Autowired private VideoService videoService;

  @Autowired private QuizService quizService;

  @Autowired private UsuarioService usuarioService;

  public Optional<Certificado> findByCodigoVerificacion(String codigo) {
    return certificadoRepository.findByCodigoVerificacion(codigo);
  }

  public Optional<Certificado> findByUsuarioAndCurso(Long usuarioId, Long cursoId) {
    return certificadoRepository.findByUsuarioIdAndCursoId(usuarioId, cursoId);
  }

  public List<Certificado> findByUsuario(Long usuarioId) {
    return certificadoRepository.findByUsuarioId(usuarioId);
  }

  public boolean yaTieneCertificado(Long usuarioId, Long cursoId) {
    return certificadoRepository.existsByUsuarioIdAndCursoId(usuarioId, cursoId);
  }

  public ElegibilidadCertificado verificarElegibilidad(Usuario usuario, Curso curso) {
    ElegibilidadCertificado elegibilidad = new ElegibilidadCertificado();

    // Verificar videos completados (100% requerido)
    List<Video> videos = videoService.findByCursoId(curso.getId());
    int totalVideos = videos.size();
    int videosCompletados = 0;

    for (Video video : videos) {
      Optional<VideoProgreso> progreso =
          videoProgresoService.findByUsernameCursoIdAndVideoId(
              usuario.getUsername(), curso.getId(), video.getId());
      if (progreso.isPresent() && progreso.get().isCompletado()) {
        videosCompletados++;
      }
    }

    elegibilidad.setTotalVideos(totalVideos);
    elegibilidad.setVideosCompletados(videosCompletados);
    elegibilidad.setVideosCompletos(totalVideos > 0 && videosCompletados >= totalVideos);

    // Verificar quizzes aprobados (nota >= 70%)
    List<Quiz> quizzes = quizService.findByCursoId(curso.getId());
    int totalQuizzes = quizzes.size();
    int quizzesAprobados = 0;
    double sumaNotas = 0;

    for (Quiz quiz : quizzes) {
      // Obtener el mejor intento del usuario para este quiz
      List<QuizIntento> intentos = quizService.findIntentos(quiz.getId(), usuario.getId());
      if (!intentos.isEmpty()) {
        // Encontrar el mejor puntaje entre todos los intentos
        double mejorNota = 0;
        for (QuizIntento intento : intentos) {
          double notaIntento =
              intento.getTotalPreguntas() > 0
                  ? (intento.getPuntaje() * 100.0 / intento.getTotalPreguntas())
                  : 0;
          if (notaIntento > mejorNota) {
            mejorNota = notaIntento;
          }
        }
        sumaNotas += mejorNota;
        if (mejorNota >= NOTA_MINIMA_APROBACION) {
          quizzesAprobados++;
        }
      }
    }

    elegibilidad.setTotalQuizzes(totalQuizzes);
    elegibilidad.setQuizzesAprobados(quizzesAprobados);
    elegibilidad.setNotaPromedio(totalQuizzes > 0 ? sumaNotas / totalQuizzes : 0);
    elegibilidad.setQuizzesCompletos(totalQuizzes == 0 || quizzesAprobados >= totalQuizzes);

    // Elegible si completó videos y aprobó quizzes
    elegibilidad.setElegible(elegibilidad.isVideosCompletos() && elegibilidad.isQuizzesCompletos());

    return elegibilidad;
  }

  private static final int DIAS_VENTANA_RENOVACION = 30;

  public Certificado generarCertificado(Usuario usuario, Curso curso) {
    ElegibilidadCertificado elegibilidad = verificarElegibilidad(usuario, curso);
    if (!elegibilidad.isElegible()) {
      return null;
    }

    Optional<Certificado> existente =
        certificadoRepository.findByUsuarioIdAndCursoId(usuario.getId(), curso.getId());

    if (existente.isPresent()) {
      Certificado cert = existente.get();
      LocalDateTime vigencia = cert.getFechaVigencia();
      boolean expirado = vigencia == null || !vigencia.isAfter(LocalDateTime.now());
      boolean proximoVencer =
          !expirado
              && ChronoUnit.DAYS.between(LocalDateTime.now(), vigencia) <= DIAS_VENTANA_RENOVACION;

      if (expirado || proximoVencer) {
        // Renovar: nueva fecha de emisión, nuevo código, estadísticas actualizadas
        cert.setFechaEmision(LocalDateTime.now());
        cert.setCodigoVerificacion(generarCodigoVerificacion());
        cert.setNotaPromedio(elegibilidad.getNotaPromedio());
        cert.setVideosCompletados(elegibilidad.getVideosCompletados());
        cert.setTotalVideos(elegibilidad.getTotalVideos());
        cert.setQuizzesAprobados(elegibilidad.getQuizzesAprobados());
        cert.setTotalQuizzes(elegibilidad.getTotalQuizzes());
        return certificadoRepository.save(cert);
      }

      // Certificado aún vigente y fuera de ventana: devolver el existente sin cambios
      return cert;
    }

    // Primer certificado
    Certificado certificado = new Certificado(usuario, curso, generarCodigoVerificacion());
    certificado.setNotaPromedio(elegibilidad.getNotaPromedio());
    certificado.setVideosCompletados(elegibilidad.getVideosCompletados());
    certificado.setTotalVideos(elegibilidad.getTotalVideos());
    certificado.setQuizzesAprobados(elegibilidad.getQuizzesAprobados());
    certificado.setTotalQuizzes(elegibilidad.getTotalQuizzes());
    return certificadoRepository.save(certificado);
  }

  private String generarCodigoVerificacion() {
    return "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  /**
   * Intenta generar automáticamente el certificado si el usuario cumple todos los requisitos.
   * Retorna el certificado si se generó, null si no cumple requisitos o ya lo tiene.
   */
  public Certificado intentarGenerarAutomaticamente(String username, Long cursoId) {
    Optional<Usuario> usuarioOpt = usuarioService.findByUsername(username);
    if (usuarioOpt.isEmpty()) {
      return null;
    }

    Usuario usuario = usuarioOpt.get();

    // Si tiene certificado vigente fuera de la ventana de renovación, no hacer nada
    Optional<Certificado> certExistente =
        certificadoRepository.findByUsuarioIdAndCursoId(usuario.getId(), cursoId);
    if (certExistente.isPresent()) {
      LocalDateTime vigencia = certExistente.get().getFechaVigencia();
      if (vigencia != null && vigencia.isAfter(LocalDateTime.now())) {
        long diasRestantes = ChronoUnit.DAYS.between(LocalDateTime.now(), vigencia);
        if (diasRestantes > DIAS_VENTANA_RENOVACION) {
          return null;
        }
      }
    }

    // Obtener el curso
    List<Video> videos = videoService.findByCursoId(cursoId);
    if (videos.isEmpty()) {
      return null;
    }
    Curso curso = videos.get(0).getCurso();

    // Verificar elegibilidad y generar si cumple
    ElegibilidadCertificado elegibilidad = verificarElegibilidad(usuario, curso);
    if (elegibilidad.isElegible()) {
      return generarCertificado(usuario, curso);
    }

    return null;
  }

  // Clase interna para el resultado de elegibilidad
  public static class ElegibilidadCertificado {
    private boolean elegible;
    private int totalVideos;
    private int videosCompletados;
    private boolean videosCompletos;
    private int totalQuizzes;
    private int quizzesAprobados;
    private boolean quizzesCompletos;
    private double notaPromedio;

    public boolean isElegible() {
      return elegible;
    }

    public void setElegible(boolean elegible) {
      this.elegible = elegible;
    }

    public int getTotalVideos() {
      return totalVideos;
    }

    public void setTotalVideos(int totalVideos) {
      this.totalVideos = totalVideos;
    }

    public int getVideosCompletados() {
      return videosCompletados;
    }

    public void setVideosCompletados(int videosCompletados) {
      this.videosCompletados = videosCompletados;
    }

    public boolean isVideosCompletos() {
      return videosCompletos;
    }

    public void setVideosCompletos(boolean videosCompletos) {
      this.videosCompletos = videosCompletos;
    }

    public int getTotalQuizzes() {
      return totalQuizzes;
    }

    public void setTotalQuizzes(int totalQuizzes) {
      this.totalQuizzes = totalQuizzes;
    }

    public int getQuizzesAprobados() {
      return quizzesAprobados;
    }

    public void setQuizzesAprobados(int quizzesAprobados) {
      this.quizzesAprobados = quizzesAprobados;
    }

    public boolean isQuizzesCompletos() {
      return quizzesCompletos;
    }

    public void setQuizzesCompletos(boolean quizzesCompletos) {
      this.quizzesCompletos = quizzesCompletos;
    }

    public double getNotaPromedio() {
      return notaPromedio;
    }

    public void setNotaPromedio(double notaPromedio) {
      this.notaPromedio = notaPromedio;
    }
  }
}
