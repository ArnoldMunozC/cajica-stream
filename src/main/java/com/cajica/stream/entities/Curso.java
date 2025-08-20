package com.cajica.stream.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Entity
@Slf4j
public class Curso {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String descripcion;
  private String categoria;

  @Column(name = "imagen_path")
  private String imagenPath;

  @Column(name = "activo", nullable = false)
  private boolean activo = true;

  @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Video> videos = new ArrayList<>();

  @ManyToMany(mappedBy = "cursosInscritos", fetch = FetchType.LAZY)
  private Set<Usuario> usuariosInscritos = new HashSet<>();

  public Curso(Long id, String nombre, String descripcion, String categoria) {
    this.id = id;
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.activo = true;
  }

  public Curso(Long id, String nombre, String descripcion, String categoria, String imagenPath) {
    this.id = id;
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.imagenPath = imagenPath;
    this.activo = true;
  }

  public Curso() {
    this.activo = true;
  }

  // Helper method to add video to the course
  public void addVideo(Video video) {
    videos.add(video);
    video.setCurso(this);
  }

  // Helper method to remove video from the course
  public void removeVideo(Video video) {
    videos.remove(video);
    video.setCurso(null);
  }

  // Helper method to safely get usuarios inscritos
  public synchronized Set<Usuario> getUsuariosInscritos() {
    return new HashSet<>(usuariosInscritos);
  }

  // Helper method to safely set usuarios inscritos
  public synchronized void setUsuariosInscritos(Set<Usuario> usuarios) {
    log.info("Actualizando lista de usuarios inscritos para el curso {}", nombre);
    this.usuariosInscritos = new HashSet<>(usuarios);
  }

  // Helper method to check if a user is enrolled
  public synchronized boolean tieneUsuarioInscrito(Long usuarioId) {
    log.info("Verificando si el usuario ID {} está inscrito en el curso {}", usuarioId, nombre);
    for (Usuario usuario : new HashSet<>(usuariosInscritos)) {
      if (usuario.getId().equals(usuarioId)) {
        log.info("El usuario ID {} está inscrito en el curso {}", usuarioId, nombre);
        return true;
      }
    }
    log.info("El usuario ID {} NO está inscrito en el curso {}", usuarioId, nombre);
    return false;
  }
}
