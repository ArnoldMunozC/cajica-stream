package com.cajica.stream.repositories;

import com.cajica.stream.entities.ContenidoProgreso;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContenidoProgresoRepository extends JpaRepository<ContenidoProgreso, Long> {

  boolean existsByUsuarioIdAndTipoAndContenidoId(Long usuarioId, String tipo, Long contenidoId);

  void deleteByTipoAndContenidoId(String tipo, Long contenidoId);

  void deleteByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);

  @Query(
      "SELECT c.contenidoId FROM ContenidoProgreso c WHERE c.usuarioId = :usuarioId AND c.cursoId ="
          + " :cursoId AND c.tipo = :tipo")
  Set<Long> findContenidoIdsByUsuarioIdAndCursoIdAndTipo(
      @Param("usuarioId") Long usuarioId,
      @Param("cursoId") Long cursoId,
      @Param("tipo") String tipo);
}
