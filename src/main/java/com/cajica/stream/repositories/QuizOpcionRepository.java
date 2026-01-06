package com.cajica.stream.repositories;

import com.cajica.stream.entities.QuizOpcion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizOpcionRepository extends JpaRepository<QuizOpcion, Long> {
  List<QuizOpcion> findByPreguntaIdOrderByOrdenAscIdAsc(Long preguntaId);
}
