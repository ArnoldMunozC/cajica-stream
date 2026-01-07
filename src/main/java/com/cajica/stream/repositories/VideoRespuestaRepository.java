package com.cajica.stream.repositories;

import com.cajica.stream.entities.VideoRespuesta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRespuestaRepository extends JpaRepository<VideoRespuesta, Long> {

  List<VideoRespuesta> findByPreguntaIdOrderByFechaCreacionAsc(Long preguntaId);
}
