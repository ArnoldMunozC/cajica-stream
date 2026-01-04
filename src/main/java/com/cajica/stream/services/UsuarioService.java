package com.cajica.stream.services;

import com.cajica.stream.dto.CursoDTO;
import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.Rol;
import com.cajica.stream.entities.Usuario;
import com.cajica.stream.repositories.CursoRepository;
import com.cajica.stream.repositories.RolRepository;
import com.cajica.stream.repositories.UsuarioRepository;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final CursoRepository cursoRepository;
  private final PasswordEncoder passwordEncoder;
  private final JdbcTemplate jdbcTemplate;
  private final RolRepository rolRepository;
  private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

  @Autowired
  public UsuarioService(
      UsuarioRepository usuarioRepository,
      CursoRepository cursoRepository,
      PasswordEncoder passwordEncoder,
      JdbcTemplate jdbcTemplate,
      RolRepository rolRepository) {
    this.usuarioRepository = usuarioRepository;
    this.cursoRepository = cursoRepository;
    this.passwordEncoder = passwordEncoder;
    this.jdbcTemplate = jdbcTemplate;
    this.rolRepository = rolRepository;
  }

  public List<Usuario> findAll() {
    return usuarioRepository.findAll();
  }

  @Transactional(readOnly = true)
  public List<Usuario> buscarPorNombreCompleto(String q) {
    if (!StringUtils.hasText(q)) {
      return usuarioRepository.findAll();
    }
    return usuarioRepository.findByNombreCompletoContainingIgnoreCase(q.trim());
  }

  public Optional<Usuario> findById(Long id) {
    return usuarioRepository.findById(id);
  }

  public Optional<Usuario> findByUsername(String username) {
    return usuarioRepository.findByUsername(username);
  }

  public Optional<Usuario> findByEmail(String email) {
    return usuarioRepository.findByEmail(email);
  }

  @Transactional
  public Usuario registrarUsuario(Usuario usuario) {
    // Codificar la contraseña antes de guardar
    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

    // Verificar si es el primer usuario para asignar rol de administrador
    boolean esPrimerUsuario = usuarioRepository.count() == 0;

    // Asignar rol según corresponda
    Rol rol;
    if (esPrimerUsuario) {
      rol =
          rolRepository
              .findByNombre("ROLE_ADMIN")
              .orElseGet(
                  () -> {
                    Rol nuevoRol = new Rol("ROLE_ADMIN");
                    return rolRepository.save(nuevoRol);
                  });
      logger.info("Asignando rol de ADMIN al primer usuario: {}", usuario.getUsername());
    } else {
      rol =
          rolRepository
              .findByNombre("ROLE_USER")
              .orElseGet(
                  () -> {
                    Rol nuevoRol = new Rol("ROLE_USER");
                    return rolRepository.save(nuevoRol);
                  });
      logger.info("Asignando rol de USER al usuario: {}", usuario.getUsername());
    }

    usuario.agregarRol(rol);
    return usuarioRepository.save(usuario);
  }

  @Transactional
  public boolean existeUsuario(String username, String email) {
    return usuarioRepository.existsByUsername(username) || usuarioRepository.existsByEmail(email);
  }

  @Transactional
  public void inscribirEnCurso(Long usuarioId, Long cursoId) {
    logger.info(
        "Iniciando proceso de inscripción: Usuario ID {} - Curso ID {}", usuarioId, cursoId);

    Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
    Optional<Curso> cursoOpt = cursoRepository.findById(cursoId);

    if (usuarioOpt.isPresent() && cursoOpt.isPresent()) {
      Usuario usuario = usuarioOpt.get();
      Curso curso = cursoOpt.get();

      logger.info(
          "Usuario y curso encontrados. Usuario: {}, Curso: {}",
          usuario.getUsername(),
          curso.getNombre());

      // Verificar si el usuario ya está inscrito
      if (usuario.estaInscritoEnCurso(cursoId)) {
        logger.info("El usuario ya está inscrito en este curso. No se realiza ninguna acción.");
        return;
      }

      try {
        logger.info("Inscribiendo usuario en curso...");
        usuario.inscribirEnCurso(curso);
        usuarioRepository.save(usuario);
        cursoRepository.save(curso);
        logger.info("Inscripción completada exitosamente");
      } catch (Exception e) {
        logger.error("Error durante la inscripción: {}", e.getMessage(), e);
        throw e;
      }
    } else {
      logger.error(
          "No se pudo completar la inscripción. Usuario existe: {}, Curso existe: {}",
          usuarioOpt.isPresent(),
          cursoOpt.isPresent());
    }
  }

  @Transactional
  public void cancelarInscripcion(Long usuarioId, Long cursoId) {
    logger.info("Cancelando inscripción: Usuario ID {} - Curso ID {}", usuarioId, cursoId);
    try {
      // Usar JdbcTemplate para ejecutar SQL directamente
      String sql = "DELETE FROM inscripciones WHERE usuario_id = ? AND curso_id = ?";
      int rowsAffected = jdbcTemplate.update(sql, usuarioId, cursoId);
      logger.info(
          "Inscripción cancelada exitosamente mediante JdbcTemplate. Filas afectadas: {}",
          rowsAffected);
    } catch (Exception e) {
      logger.error("Error al cancelar inscripción mediante JdbcTemplate: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Transactional
  public void inscribirEnCursoJdbc(Long usuarioId, Long cursoId) {
    logger.info("Inscribiendo mediante JDBC: Usuario ID {} - Curso ID {}", usuarioId, cursoId);

    // Verificar si el usuario ya está inscrito usando SQL directo
    String checkSql = "SELECT COUNT(*) FROM inscripciones WHERE usuario_id = ? AND curso_id = ?";
    int count = jdbcTemplate.queryForObject(checkSql, Integer.class, usuarioId, cursoId);

    if (count > 0) {
      logger.info("El usuario ya está inscrito en este curso. No se realiza ninguna acción.");
      return;
    }

    // Verificar que el usuario y el curso existen
    String checkUsuarioSql = "SELECT COUNT(*) FROM usuarios WHERE id = ?";
    String checkCursoSql = "SELECT COUNT(*) FROM curso WHERE id = ? AND activo = true";

    int usuarioExists = jdbcTemplate.queryForObject(checkUsuarioSql, Integer.class, usuarioId);
    int cursoExists = jdbcTemplate.queryForObject(checkCursoSql, Integer.class, cursoId);

    if (usuarioExists == 0 || cursoExists == 0) {
      logger.error(
          "No se pudo completar la inscripción. Usuario existe: {}, Curso activo existe: {}",
          usuarioExists > 0,
          cursoExists > 0);
      throw new RuntimeException(
          "No se pudo completar la inscripción. Usuario o curso no encontrado o curso"
              + " desactivado.");
    }

    try {
      // Insertar directamente en la tabla de inscripciones
      String insertSql = "INSERT INTO inscripciones (usuario_id, curso_id) VALUES (?, ?)";
      int rowsAffected = jdbcTemplate.update(insertSql, usuarioId, cursoId);
      logger.info(
          "Inscripción completada exitosamente mediante JdbcTemplate. Filas afectadas: {}",
          rowsAffected);
    } catch (Exception e) {
      logger.error("Error durante la inscripción mediante JdbcTemplate: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Transactional(readOnly = true)
  public boolean estaInscrito(Long usuarioId, Long cursoId) {
    // Primero intentamos verificar desde el lado del usuario
    Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
    if (usuarioOpt.isPresent()) {
      Usuario usuario = usuarioOpt.get();
      if (usuario.estaInscritoEnCurso(cursoId)) {
        return true;
      }
    }

    // Si no se encuentra desde el usuario, verificamos desde el curso
    Optional<Curso> cursoOpt = cursoRepository.findById(cursoId);
    if (cursoOpt.isPresent()) {
      Curso curso = cursoOpt.get();
      return curso.tieneUsuarioInscrito(usuarioId);
    }

    return false;
  }

  @Transactional(readOnly = true)
  public Set<Curso> getCursosInscritos(Long usuarioId) {
    Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

    if (usuarioOpt.isPresent()) {
      Usuario usuario = usuarioOpt.get();
      return usuario.getCursosInscritos();
    }

    return Set.of(); // Conjunto vacío
  }

  /**
   * Método seguro para obtener los cursos a los que un usuario está inscrito. Utiliza JdbcTemplate
   * para consultar directamente la base de datos en lugar de navegar por las colecciones, evitando
   * así problemas de ConcurrentModificationException.
   *
   * @param usuarioId ID del usuario
   * @return Set de cursos a los que el usuario está inscrito
   */
  @Transactional(readOnly = true)
  public Set<Curso> getCursosInscritosDirecto(Long usuarioId) {
    logger.info("Obteniendo cursos inscritos para el usuario ID: {}", usuarioId);
    try {
      // Consulta SQL para obtener los cursos activos a los que el usuario está inscrito
      String sql =
          "SELECT c.* FROM curso c "
              + "JOIN inscripciones i ON c.id = i.curso_id "
              + "WHERE i.usuario_id = ? AND c.activo = true";

      logger.info("Ejecutando consulta SQL: {}", sql);
      logger.info("Parámetro usuarioId: {}", usuarioId);

      // Ejecutar la consulta y mapear los resultados a objetos CursoDTO
      List<CursoDTO> cursosDTO =
          jdbcTemplate.query(
              sql,
              (rs, rowNum) -> {
                Long id = rs.getLong("id");
                String nombre = rs.getString("nombre");
                String descripcion = rs.getString("descripcion");
                String categoria = rs.getString("categoria");
                String imagenPath = rs.getString("imagen_path");

                logger.info("Curso encontrado - ID: {}, Nombre: {}", id, nombre);

                return new CursoDTO(id, nombre, descripcion, categoria, imagenPath);
              },
              usuarioId);

      logger.info(
          "Se encontraron {} cursos inscritos para el usuario ID: {}", cursosDTO.size(), usuarioId);

      // Imprimir detalles de cada curso encontrado
      for (CursoDTO cursoDTO : cursosDTO) {
        logger.info("Curso: ID={}, Nombre={}", cursoDTO.getId(), cursoDTO.getNombre());
      }

      // Convertir los DTOs a entidades Curso
      Set<Curso> cursos = new HashSet<>();
      for (CursoDTO dto : cursosDTO) {
        Curso curso =
            new Curso(
                dto.getId(),
                dto.getNombre(),
                dto.getDescripcion(),
                dto.getCategoria(),
                dto.getImagenPath());
        cursos.add(curso);
      }

      return cursos;
    } catch (Exception e) {
      logger.error(
          "Error al obtener cursos inscritos mediante JdbcTemplate: {}", e.getMessage(), e);
      logger.error("Tipo de excepción: {}", e.getClass().getName());
      logger.error("Stack trace: ", e);
      return Set.of(); // Devolver conjunto vacío en caso de error
    }
  }

  /**
   * Método para obtener los datos crudos de los cursos a los que un usuario está inscrito. Este
   * método devuelve los datos como una lista de mapas, lo que evita problemas de mapeo a entidades
   * y concurrencia.
   *
   * @param usuarioId ID del usuario
   * @return Lista de mapas con los datos de los cursos
   */
  public List<Map<String, Object>> getCursosInscritosRaw(Long usuarioId) {
    logger.info("Obteniendo datos crudos de cursos inscritos para el usuario ID: {}", usuarioId);
    try {
      // Consulta SQL para obtener los cursos a los que el usuario está inscrito
      String sql =
          "SELECT c.id, c.nombre, c.descripcion, c.categoria, c.imagen_path "
              + "FROM curso c "
              + "JOIN inscripciones i ON c.id = i.curso_id "
              + "WHERE i.usuario_id = ? AND c.activo = true";

      logger.info("Ejecutando consulta SQL: {}", sql);

      // Ejecutar la consulta y obtener los resultados como mapas
      List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql, usuarioId);

      logger.info(
          "Se encontraron {} cursos inscritos para el usuario ID: {}",
          resultados.size(),
          usuarioId);

      return resultados;
    } catch (Exception e) {
      logger.error("Error al obtener datos crudos de cursos inscritos: {}", e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  @Transactional
  public void actualizarUsuario(Usuario usuario) {
    usuarioRepository.save(usuario);
  }

  @Transactional
  public void actualizarEmailYActivo(Long usuarioId, String email, boolean activo) {
    if (!StringUtils.hasText(email)) {
      throw new IllegalArgumentException("El email no puede estar vacío");
    }

    String normalizedEmail = email.trim();
    if (usuarioRepository.existsByEmailAndIdNot(normalizedEmail, usuarioId)) {
      throw new IllegalArgumentException("Ya existe un usuario con ese email");
    }

    Usuario usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    usuario.setEmail(normalizedEmail);
    usuario.setActivo(activo);
    usuarioRepository.save(usuario);
  }

  @Transactional
  public void eliminarUsuario(Long id) {
    usuarioRepository.deleteById(id);
  }

  /**
   * Método seguro para verificar si un usuario está inscrito en un curso. Este método está diseñado
   * para ser llamado de manera asíncrona y evitar problemas de ConcurrentModificationException.
   *
   * @param username Nombre de usuario
   * @param cursoId ID del curso
   * @return true si el usuario está inscrito, false en caso contrario
   */
  @Transactional(readOnly = true)
  public boolean verificarInscripcionSegura(String username, Long cursoId) {
    // Usar consultas directas a la base de datos en lugar de navegar por las colecciones
    return usuarioRepository.existsByUsernameAndCursosInscritosId(username, cursoId);
  }

  /**
   * Verifica si un usuario tiene inscripciones en algún curso.
   *
   * @param usuarioId ID del usuario
   * @return true si el usuario tiene al menos una inscripción, false en caso contrario
   */
  public boolean verificarSiTieneInscripciones(Long usuarioId) {
    logger.info("Verificando si el usuario ID {} tiene inscripciones", usuarioId);
    try {
      String sql = "SELECT COUNT(*) FROM inscripciones WHERE usuario_id = ?";
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class, usuarioId);

      boolean tieneInscripciones = count != null && count > 0;
      logger.info("Usuario ID {}: {} inscripciones encontradas", usuarioId, count);
      return tieneInscripciones;
    } catch (Exception e) {
      logger.error(
          "Error al verificar inscripciones del usuario {}: {}", usuarioId, e.getMessage(), e);
      return false;
    }
  }

  /**
   * Genera un token de recuperación de contraseña para un usuario
   *
   * @param email Email del usuario
   * @return true si se generó el token correctamente, false en caso contrario
   */
  @Transactional
  public boolean generarTokenRecuperacion(String email) {
    logger.info("Generando token de recuperación para email: {}", email);
    Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

    if (usuarioOpt.isEmpty()) {
      logger.warn("No se encontró usuario con email: {}", email);
      return false;
    }

    Usuario usuario = usuarioOpt.get();

    // Generar token aleatorio
    String token = UUID.randomUUID().toString();

    // Establecer fecha de expiración (24 horas)
    java.time.LocalDateTime expiry = java.time.LocalDateTime.now().plusHours(24);

    // Guardar token y fecha de expiración
    usuario.setResetPasswordToken(token);
    usuario.setResetPasswordTokenExpiry(expiry);
    usuarioRepository.save(usuario);

    logger.info("Token de recuperación generado para usuario: {}", usuario.getUsername());
    return true;
  }

  /**
   * Valida un token de recuperación de contraseña
   *
   * @param token Token a validar
   * @return Usuario asociado al token si es válido, Optional.empty() en caso contrario
   */
  @Transactional(readOnly = true)
  public Optional<Usuario> validarTokenRecuperacion(String token) {
    logger.info("Validando token de recuperación: {}", token);

    if (token == null || token.isEmpty()) {
      logger.warn("Token vacío o nulo");
      return Optional.empty();
    }

    // Buscar usuario por token
    Optional<Usuario> usuarioOpt = usuarioRepository.findByResetPasswordToken(token);

    if (usuarioOpt.isEmpty()) {
      logger.warn("No se encontró usuario con el token proporcionado");
      return Optional.empty();
    }

    Usuario usuario = usuarioOpt.get();

    // Verificar si el token ha expirado
    if (usuario.getResetPasswordTokenExpiry() == null
        || usuario.getResetPasswordTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
      logger.warn("Token expirado para usuario: {}", usuario.getUsername());
      return Optional.empty();
    }

    logger.info("Token válido para usuario: {}", usuario.getUsername());
    return usuarioOpt;
  }

  /**
   * Actualiza la contraseña de un usuario usando un token de recuperación
   *
   * @param token Token de recuperación
   * @param newPassword Nueva contraseña
   * @return true si se actualizó correctamente, false en caso contrario
   */
  @Transactional
  public boolean actualizarContraseña(String token, String newPassword) {
    logger.info("Actualizando contraseña con token de recuperación");

    Optional<Usuario> usuarioOpt = validarTokenRecuperacion(token);

    if (usuarioOpt.isEmpty()) {
      logger.warn("Token inválido o expirado");
      return false;
    }

    Usuario usuario = usuarioOpt.get();

    // Encriptar la nueva contraseña
    String encryptedPassword = passwordEncoder.encode(newPassword);
    usuario.setPassword(encryptedPassword);

    // Limpiar el token y la fecha de expiración
    usuario.setResetPasswordToken(null);
    usuario.setResetPasswordTokenExpiry(null);

    usuarioRepository.save(usuario);
    logger.info("Contraseña actualizada exitosamente para usuario: {}", usuario.getUsername());

    return true;
  }
}
