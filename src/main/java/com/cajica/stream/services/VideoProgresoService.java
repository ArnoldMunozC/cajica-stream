package com.cajica.stream.services;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.Usuario;
import com.cajica.stream.entities.Video;
import com.cajica.stream.entities.VideoProgreso;
import com.cajica.stream.repositories.CursoRepository;
import com.cajica.stream.repositories.UsuarioRepository;
import com.cajica.stream.repositories.VideoProgresoRepository;
import com.cajica.stream.repositories.VideoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VideoProgresoService {

  private final VideoProgresoRepository videoProgresoRepository;
  private final UsuarioRepository usuarioRepository;
  private final CursoRepository cursoRepository;
  private final VideoRepository videoRepository;

  public VideoProgresoService(
      VideoProgresoRepository videoProgresoRepository,
      UsuarioRepository usuarioRepository,
      CursoRepository cursoRepository,
      VideoRepository videoRepository) {
    this.videoProgresoRepository = videoProgresoRepository;
    this.usuarioRepository = usuarioRepository;
    this.cursoRepository = cursoRepository;
    this.videoRepository = videoRepository;
  }

  @Transactional(readOnly = true)
  public Optional<VideoProgreso> findUltimoByUsernameAndCursoId(String username, Long cursoId) {
    Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
    if (usuario.isEmpty()) {
      return Optional.empty();
    }
    return videoProgresoRepository.findTopByUsuarioIdAndCursoIdOrderByUpdatedAtDesc(
        usuario.get().getId(), cursoId);
  }

  @Transactional(readOnly = true)
  public Optional<VideoProgreso> findByUsernameCursoIdAndVideoId(
      String username, Long cursoId, Long videoId) {
    Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
    if (usuario.isEmpty()) {
      return Optional.empty();
    }
    return videoProgresoRepository.findByUsuarioIdAndCursoIdAndVideoId(
        usuario.get().getId(), cursoId, videoId);
  }

  @Transactional(readOnly = true)
  public List<VideoProgreso> findCompletadosByUsernameAndCursoId(String username, Long cursoId) {
    Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
    if (usuario.isEmpty()) {
      return java.util.Collections.emptyList();
    }
    return videoProgresoRepository.findByUsuarioIdAndCursoIdAndCompletadoTrue(
        usuario.get().getId(), cursoId);
  }

  @Transactional
  public void upsertProgreso(String username, Long cursoId, Long videoId, Double segundo) {
    Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
    if (usuarioOpt.isEmpty()) {
      return;
    }

    Long usuarioId = usuarioOpt.get().getId();

    VideoProgreso progreso =
        videoProgresoRepository
            .findByUsuarioIdAndCursoIdAndVideoId(usuarioId, cursoId, videoId)
            .orElseGet(VideoProgreso::new);

    Usuario usuarioRef = usuarioRepository.getReferenceById(usuarioId);
    Curso cursoRef = cursoRepository.getReferenceById(cursoId);
    Video videoRef = videoRepository.getReferenceById(videoId);

    progreso.setUsuario(usuarioRef);
    progreso.setCurso(cursoRef);
    progreso.setVideo(videoRef);
    progreso.setSegundo(segundo == null || segundo < 0 ? 0 : segundo);

    videoProgresoRepository.save(progreso);
  }

  @Transactional
  public void marcarCompletado(String username, Long cursoId, Long videoId) {
    Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
    if (usuarioOpt.isEmpty()) {
      return;
    }

    Long usuarioId = usuarioOpt.get().getId();

    VideoProgreso progreso =
        videoProgresoRepository
            .findByUsuarioIdAndCursoIdAndVideoId(usuarioId, cursoId, videoId)
            .orElseGet(VideoProgreso::new);

    Usuario usuarioRef = usuarioRepository.getReferenceById(usuarioId);
    Curso cursoRef = cursoRepository.getReferenceById(cursoId);
    Video videoRef = videoRepository.getReferenceById(videoId);

    progreso.setUsuario(usuarioRef);
    progreso.setCurso(cursoRef);
    progreso.setVideo(videoRef);
    if (progreso.getSegundo() == null) {
      progreso.setSegundo(0d);
    }

    if (!progreso.isCompletado()) {
      progreso.setCompletado(true);
      progreso.setFechaCompletado(LocalDateTime.now());
    }

    videoProgresoRepository.save(progreso);
  }
}
