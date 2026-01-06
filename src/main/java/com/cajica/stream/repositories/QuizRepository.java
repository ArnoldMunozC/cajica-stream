package com.cajica.stream.repositories;

import com.cajica.stream.entities.Quiz;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
  List<Quiz> findByCursoId(Long cursoId);

  @Query(
      "select q from Quiz q where q.curso.id = :cursoId and ((:seccion is null and q.seccion is"
          + " null) or q.seccion = :seccion)")
  Optional<Quiz> findByCursoIdAndSeccion(
      @Param("cursoId") Long cursoId, @Param("seccion") String seccion);
}
