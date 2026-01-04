package com.cajica.stream.repositories;

import com.cajica.stream.entities.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

  Optional<Usuario> findByUsername(String username);

  Optional<Usuario> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  Optional<Usuario> findByResetPasswordToken(String token);

  List<Usuario> findByNombreCompletoContainingIgnoreCase(String nombreCompleto);

  boolean existsByEmailAndIdNot(String email, Long id);

  /**
   * Verifica si existe un usuario con el nombre de usuario dado que esté inscrito en el curso con
   * el ID especificado. Este método es más seguro que navegar por las colecciones directamente,
   * evitando ConcurrentModificationException.
   *
   * @param username Nombre de usuario
   * @param cursoId ID del curso
   * @return true si el usuario está inscrito en el curso, false en caso contrario
   */
  boolean existsByUsernameAndCursosInscritosId(String username, Long cursoId);

  /**
   * Cancela la inscripción de un usuario a un curso usando SQL nativo. Este método es más seguro
   * que manipular colecciones en entidades JPA.
   *
   * @param usuarioId ID del usuario que se va a cancelar la inscripción
   * @param cursoId ID del curso al que se va a cancelar la inscripción
   */
  @Modifying
  @Transactional
  @Query(
      value = "DELETE FROM inscripciones WHERE usuario_id = :usuarioId AND curso_id = :cursoId",
      nativeQuery = true)
  void cancelarInscripcionDirecta(Long usuarioId, Long cursoId);
}
