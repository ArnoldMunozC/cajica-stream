package com.cajica.stream.repositories;

import com.cajica.stream.entities.Video;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

  // Encontrar videos por curso_id
  List<Video> findByCursoIdOrderByOrdenAsc(Long cursoId);

  // Contar videos por curso_id
  Long countByCursoId(Long cursoId);
}
