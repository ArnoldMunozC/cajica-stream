package com.cajica.stream.repositories;

import com.cajica.stream.entities.QuizRespuesta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface QuizRespuestaRepository extends JpaRepository<QuizRespuesta, Long> {
  List<QuizRespuesta> findByIntentoId(Long intentoId);

  @Transactional
  @Modifying
  @Query(
      "DELETE FROM QuizRespuesta qr WHERE qr.intento.id IN (SELECT qi.id FROM QuizIntento qi WHERE"
          + " qi.usuario.id = :usuarioId AND qi.quiz.curso.id = :cursoId)")
  void deleteByUsuarioIdAndCursoId(
      @Param("usuarioId") Long usuarioId, @Param("cursoId") Long cursoId);
}
