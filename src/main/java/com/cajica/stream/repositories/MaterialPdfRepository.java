package com.cajica.stream.repositories;

import com.cajica.stream.entities.MaterialPdf;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialPdfRepository extends JpaRepository<MaterialPdf, Long> {
  @Query(
      "select p from MaterialPdf p where p.curso.id = :cursoId order by case when p.orden is null"
          + " then 1 else 0 end, p.orden asc, p.id asc")
  List<MaterialPdf> findByCursoIdOrderByOrdenAscIdAsc(@Param("cursoId") Long cursoId);

  Long countByCursoId(Long cursoId);

  @Query(
      "select max(p.orden) from MaterialPdf p where p.curso.id = :cursoId and ((:seccion is null"
          + " and p.seccion is null) or p.seccion = :seccion)")
  Integer findMaxOrdenByCursoIdAndSeccion(
      @Param("cursoId") Long cursoId, @Param("seccion") String seccion);

  @Query(
      "select p from MaterialPdf p where p.curso.id = :cursoId and ((:seccion is null and p.seccion"
          + " is null) or p.seccion = :seccion) and p.orden >= :ordenMin order by p.orden desc,"
          + " p.id desc")
  List<MaterialPdf> findByCursoIdAndSeccionWithOrdenGteOrderByOrdenDescIdDesc(
      @Param("cursoId") Long cursoId,
      @Param("seccion") String seccion,
      @Param("ordenMin") Integer ordenMin);
}
