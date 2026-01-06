package com.cajica.stream.repositories;

import com.cajica.stream.entities.QuizIntento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizIntentoRepository extends JpaRepository<QuizIntento, Long> {
  Long countByQuizIdAndUsuarioId(Long quizId, Long usuarioId);

  Optional<QuizIntento> findTopByQuizIdAndUsuarioIdOrderByNumeroIntentoDesc(
      Long quizId, Long usuarioId);

  List<QuizIntento> findByQuizIdAndUsuarioIdOrderByNumeroIntentoDesc(Long quizId, Long usuarioId);
}
