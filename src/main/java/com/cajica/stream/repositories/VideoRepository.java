package com.cajica.stream.repositories;

import com.cajica.stream.entities.Video;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

  // Encontrar videos por curso_id
  List<Video> findByCursoIdOrderByOrdenAsc(Long cursoId);

  // Contar videos por curso_id
  Long countByCursoId(Long cursoId);

  @Query(
      "select max(v.orden) from Video v where v.curso.id = :cursoId and ((:seccion is null and"
          + " v.seccion is null) or v.seccion = :seccion)")
  Integer findMaxOrdenByCursoIdAndSeccion(
      @Param("cursoId") Long cursoId, @Param("seccion") String seccion);

  @Query(
      "select v from Video v where v.curso.id = :cursoId and ((:seccion is null and v.seccion is"
          + " null) or v.seccion = :seccion) and v.orden >= :ordenMin order by v.orden desc, v.id"
          + " desc")
  List<Video> findByCursoIdAndSeccionWithOrdenGteOrderByOrdenDescIdDesc(
      @Param("cursoId") Long cursoId,
      @Param("seccion") String seccion,
      @Param("ordenMin") Integer ordenMin);
}
