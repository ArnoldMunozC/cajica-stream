package com.cajica.stream.repositories;

import com.cajica.stream.entities.VideoPregunta;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VideoPreguntaRepository extends JpaRepository<VideoPregunta, Long> {

  @EntityGraph(attributePaths = {"usuario", "respuestas", "respuestas.usuario"})
  List<VideoPregunta> findByVideoIdOrderByFechaCreacionDesc(Long videoId);

  @EntityGraph(attributePaths = {"curso", "video", "usuario", "respuestas", "respuestas.usuario"})
  @Query(
      "select p from VideoPregunta p "
          + "where not exists ("
          + "  select r.id from VideoRespuesta r "
          + "  where r.pregunta = p and r.esInstructor = true and r.visible = true"
          + ") "
          + "order by p.fechaCreacion desc")
  List<VideoPregunta> findPendientesOrderByFechaCreacionDesc();

  @Query(
      "select count(p) from VideoPregunta p "
          + "where not exists ("
          + "  select r.id from VideoRespuesta r "
          + "  where r.pregunta = p and r.esInstructor = true and r.visible = true"
          + ")")
  long countPendientes();
}
