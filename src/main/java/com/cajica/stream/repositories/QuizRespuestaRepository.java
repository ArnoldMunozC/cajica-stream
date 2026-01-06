package com.cajica.stream.repositories;

import com.cajica.stream.entities.QuizRespuesta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRespuestaRepository extends JpaRepository<QuizRespuesta, Long> {
  List<QuizRespuesta> findByIntentoId(Long intentoId);
}
