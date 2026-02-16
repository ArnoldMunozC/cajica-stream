package com.cajica.stream.repositories;

import com.cajica.stream.entities.Certificado;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Long> {

  Optional<Certificado> findByCodigoVerificacion(String codigoVerificacion);

  Optional<Certificado> findByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);

  List<Certificado> findByUsuarioId(Long usuarioId);

  List<Certificado> findByCursoId(Long cursoId);

  boolean existsByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);
}
