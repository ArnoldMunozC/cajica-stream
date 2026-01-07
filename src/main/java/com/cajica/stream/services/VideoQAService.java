package com.cajica.stream.services;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.Usuario;
import com.cajica.stream.entities.Video;
import com.cajica.stream.entities.VideoPregunta;
import com.cajica.stream.entities.VideoRespuesta;
import com.cajica.stream.repositories.CursoRepository;
import com.cajica.stream.repositories.UsuarioRepository;
import com.cajica.stream.repositories.VideoPreguntaRepository;
import com.cajica.stream.repositories.VideoRepository;
import com.cajica.stream.repositories.VideoRespuestaRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class VideoQAService {

  private final VideoPreguntaRepository videoPreguntaRepository;
  private final VideoRespuestaRepository videoRespuestaRepository;
  private final UsuarioRepository usuarioRepository;
  private final CursoRepository cursoRepository;
  private final VideoRepository videoRepository;
  private final UsuarioService usuarioService;

  @Autowired
  public VideoQAService(
      VideoPreguntaRepository videoPreguntaRepository,
      VideoRespuestaRepository videoRespuestaRepository,
      UsuarioRepository usuarioRepository,
      CursoRepository cursoRepository,
      VideoRepository videoRepository,
      UsuarioService usuarioService) {
    this.videoPreguntaRepository = videoPreguntaRepository;
    this.videoRespuestaRepository = videoRespuestaRepository;
    this.usuarioRepository = usuarioRepository;
    this.cursoRepository = cursoRepository;
    this.videoRepository = videoRepository;
    this.usuarioService = usuarioService;
  }

  @Transactional(readOnly = true)
  public List<VideoPregunta> findPreguntasByVideoId(Long videoId) {
    return videoPreguntaRepository.findByVideoIdOrderByFechaCreacionDesc(videoId);
  }

  @Transactional(readOnly = true)
  public List<VideoPregunta> findPreguntasPendientes() {
    return videoPreguntaRepository.findPendientesOrderByFechaCreacionDesc();
  }

  @Transactional(readOnly = true)
  public long countPreguntasPendientes() {
    return videoPreguntaRepository.countPendientes();
  }

  @Transactional
  public VideoPregunta crearPregunta(
      String username,
      boolean isAdmin,
      Long cursoId,
      Long videoId,
      String titulo,
      String contenido) {

    if (!StringUtils.hasText(titulo) || !StringUtils.hasText(contenido)) {
      throw new IllegalArgumentException("Título y contenido son obligatorios");
    }

    if (!isAdmin) {
      boolean inscrito = usuarioService.verificarInscripcionSegura(username, cursoId);
      if (!inscrito) {
        throw new IllegalStateException(
            "Necesitas estar inscrito en este curso para hacer una pregunta");
      }
    }

    Usuario usuario =
        usuarioRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    Curso curso =
        cursoRepository
            .findById(cursoId)
            .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

    Video video =
        videoRepository
            .findById(videoId)
            .orElseThrow(() -> new IllegalArgumentException("Video no encontrado"));

    if (video.getCurso() == null || !video.getCurso().getId().equals(cursoId)) {
      throw new IllegalArgumentException("El video no pertenece al curso");
    }

    VideoPregunta pregunta = new VideoPregunta();
    pregunta.setCurso(curso);
    pregunta.setVideo(video);
    pregunta.setUsuario(usuario);
    pregunta.setTitulo(titulo.trim());
    pregunta.setContenido(contenido.trim());

    return videoPreguntaRepository.save(pregunta);
  }

  @Transactional
  public VideoRespuesta crearRespuesta(
      String username,
      boolean isAdmin,
      Long cursoId,
      Long videoId,
      Long preguntaId,
      String contenido) {

    if (!StringUtils.hasText(contenido)) {
      throw new IllegalArgumentException("La respuesta no puede estar vacía");
    }

    if (!isAdmin) {
      boolean inscrito = usuarioService.verificarInscripcionSegura(username, cursoId);
      if (!inscrito) {
        throw new IllegalStateException("Necesitas estar inscrito en este curso para responder");
      }
    }

    Usuario usuario =
        usuarioRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    VideoPregunta pregunta =
        videoPreguntaRepository
            .findById(preguntaId)
            .orElseThrow(() -> new IllegalArgumentException("Pregunta no encontrada"));

    if (pregunta.getCurso() == null || !pregunta.getCurso().getId().equals(cursoId)) {
      throw new IllegalArgumentException("La pregunta no pertenece al curso");
    }

    if (pregunta.getVideo() == null || !pregunta.getVideo().getId().equals(videoId)) {
      throw new IllegalArgumentException("La pregunta no pertenece al video");
    }

    if (pregunta.isCerrada()) {
      throw new IllegalStateException("Esta pregunta está cerrada");
    }

    VideoRespuesta respuesta = new VideoRespuesta();
    respuesta.setPregunta(pregunta);
    respuesta.setUsuario(usuario);
    respuesta.setContenido(contenido.trim());
    respuesta.setEsInstructor(isAdmin);

    return videoRespuestaRepository.save(respuesta);
  }
}
