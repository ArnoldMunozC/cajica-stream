package com.cajica.stream.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Entity
@Slf4j
@Table(name = "usuarios")
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "nombre_completo")
  private String nombreCompleto;

  @Column(name = "tipo_documento", length = 30)
  private String tipoDocumento;

  @Column(name = "numero_identificacion", length = 20)
  private String numeroIdentificacion;

  @Column(name = "edad")
  private Integer edad;

  @Column(name = "enfoque_genero", length = 20)
  private String enfoqueGenero;

  @Column(name = "clasificacion_poblacional", length = 50)
  private String clasificacionPoblacional;

  @Column(name = "zona_residencia", length = 10)
  private String zonaResidencia;

  @Column(name = "direccion")
  private String direccion;

  @Column(name = "telefono", length = 20)
  private String telefono;

  @Column(name = "activo")
  private boolean activo = true;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "inscripciones",
      joinColumns = @JoinColumn(name = "usuario_id"),
      inverseJoinColumns = @JoinColumn(name = "curso_id"))
  private Set<Curso> cursosInscritos = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "usuarios_roles",
      joinColumns = @JoinColumn(name = "usuario_id"),
      inverseJoinColumns = @JoinColumn(name = "rol_id"))
  private Set<Rol> roles = new HashSet<>();

  @Column(name = "reset_password_token")
  private String resetPasswordToken;

  @Column(name = "reset_password_token_expiry")
  private LocalDateTime resetPasswordTokenExpiry;

  // Constructor vacío
  public Usuario() {}

  // Constructor con campos básicos
  public Usuario(String username, String password, String email, String nombreCompleto) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.nombreCompleto = nombreCompleto;
  }

  // Métodos helper para manejar inscripciones
  public synchronized void inscribirEnCurso(Curso curso) {
    log.info("Inscribiendo al usuario {} en el curso {}", username, curso.getNombre());
    try {
      // Usar un enfoque defensivo para evitar ConcurrentModificationException
      Set<Curso> cursosCopy = new HashSet<>(cursosInscritos);
      cursosCopy.add(curso);
      cursosInscritos = cursosCopy;

      // Actualizar el otro lado de la relación bidireccional de manera segura
      Set<Usuario> usuariosInscritos =
          curso.getUsuariosInscritos(); // Ya devuelve una copia defensiva
      usuariosInscritos.add(this);
      curso.setUsuariosInscritos(usuariosInscritos);
      log.info("Inscripción exitosa");
    } catch (Exception e) {
      log.error("Error durante la inscripción: {}", e.getMessage(), e);
      throw e;
    }
  }

  public synchronized void cancelarInscripcion(Curso curso) {
    log.info("Cancelando inscripción del usuario {} en el curso {}", username, curso.getNombre());
    // Crear una copia para evitar ConcurrentModificationException
    Set<Curso> cursosCopy = new HashSet<>(cursosInscritos);
    cursosCopy.remove(curso);
    cursosInscritos = cursosCopy;
    log.info("Inscripción cancelada");
  }

  public synchronized boolean estaInscritoEnCurso(Long cursoId) {
    log.info("Verificando si el usuario {} está inscrito en el curso {}", username, cursoId);
    // Usar un enfoque más seguro para evitar ConcurrentModificationException
    for (Curso curso : new HashSet<>(cursosInscritos)) {
      if (curso.getId().equals(cursoId)) {
        log.info("El usuario {} está inscrito en el curso {}", username, cursoId);
        return true;
      }
    }
    log.info("El usuario {} no está inscrito en el curso {}", username, cursoId);
    return false;
  }

  // Métodos helper para manejar roles
  public void agregarRol(Rol rol) {
    roles.add(rol);
  }

  public void quitarRol(Rol rol) {
    roles.remove(rol);
  }

  public boolean tieneRol(String nombreRol) {
    for (Rol rol : roles) {
      if (rol.getNombre().equals(nombreRol)) {
        return true;
      }
    }
    return false;
  }
}
