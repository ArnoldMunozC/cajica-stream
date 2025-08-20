package com.cajica.stream.repositories;

import com.cajica.stream.entities.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
  // Aquí puedes añadir métodos personalizados de consulta si los necesitas
}
