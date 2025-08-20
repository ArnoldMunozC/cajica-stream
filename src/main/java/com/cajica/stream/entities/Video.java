package com.cajica.stream.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Video {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String titulo;

  @Column(length = 1000)
  private String descripcion;

  @Column(name = "video_url")
  private String videoUrl;

  @Column(name = "video_file_path")
  private String videoFilePath;

  @Column(name = "thumbnail_path")
  private String thumbnailPath;

  private Integer duracion; // Duración en segundos

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "curso_id")
  private Curso curso;

  @Column(name = "orden")
  private Integer orden; // Para ordenar los videos dentro del curso

  public Video() {}

  public Video(String titulo, String descripcion, String videoUrl, Curso curso) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.videoUrl = videoUrl;
    this.curso = curso;
  }

  public Video(
      String titulo,
      String descripcion,
      String videoUrl,
      String videoFilePath,
      String thumbnailPath,
      Integer duracion,
      Curso curso,
      Integer orden) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.videoUrl = videoUrl;
    this.videoFilePath = videoFilePath;
    this.thumbnailPath = thumbnailPath;
    this.duracion = duracion;
    this.curso = curso;
    this.orden = orden;
  }
}
