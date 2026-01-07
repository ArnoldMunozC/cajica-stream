package com.cajica.stream.repositories;

import com.cajica.stream.entities.VideoProgreso;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoProgresoRepository extends JpaRepository<VideoProgreso, Long> {
  Optional<VideoProgreso> findByUsuarioIdAndCursoIdAndVideoId(
      Long usuarioId, Long cursoId, Long videoId);

  Optional<VideoProgreso> findTopByUsuarioIdAndCursoIdOrderByUpdatedAtDesc(
      Long usuarioId, Long cursoId);

  List<VideoProgreso> findByUsuarioIdAndCursoIdAndCompletadoTrue(Long usuarioId, Long cursoId);
}
