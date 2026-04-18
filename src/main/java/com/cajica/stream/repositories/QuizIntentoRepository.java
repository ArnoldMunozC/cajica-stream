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

  List<QuizIntento> findByQuizIdAndUsuarioIdOrderByFechaCreacionAsc(Long quizId, Long usuarioId);

  List<QuizIntento> findByQuizId(Long quizId);

  @org.springframework.data.jpa.repository.Query(
      "SELECT DISTINCT qi.quiz.id FROM QuizIntento qi WHERE qi.usuario.id = :usuarioId AND"
          + " qi.quiz.curso.id = :cursoId AND qi.aprobado = true")
  java.util.Set<Long> findQuizIdsAprobadosByUsuarioIdAndCursoId(
      @org.springframework.data.repository.query.Param("usuarioId") Long usuarioId,
      @org.springframework.data.repository.query.Param("cursoId") Long cursoId);

  @org.springframework.data.jpa.repository.Query(
      "SELECT DISTINCT qi.quiz.id FROM QuizIntento qi "
          + "WHERE qi.usuario.id = :usuarioId AND qi.quiz.curso.id = :cursoId")
  java.util.Set<Long> findQuizIdsIntentatosByUsuarioIdAndCursoId(
      @org.springframework.data.repository.query.Param("usuarioId") Long usuarioId,
      @org.springframework.data.repository.query.Param("cursoId") Long cursoId);
}
