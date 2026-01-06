package com.cajica.stream.repositories;

import com.cajica.stream.entities.QuizPregunta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizPreguntaRepository extends JpaRepository<QuizPregunta, Long> {
  List<QuizPregunta> findByQuizIdOrderByOrdenAscIdAsc(Long quizId);
}
