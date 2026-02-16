package com.cajica.stream.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "certificados")
public class Certificado {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "codigo_verificacion", nullable = false, unique = true, length = 50)
  private String codigoVerificacion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "curso_id", nullable = false)
  private Curso curso;

  @Column(name = "fecha_emision", nullable = false)
  private LocalDateTime fechaEmision;

  @Column(name = "nota_promedio")
  private Double notaPromedio;

  @Column(name = "videos_completados")
  private Integer videosCompletados;

  @Column(name = "total_videos")
  private Integer totalVideos;

  @Column(name = "quizzes_aprobados")
  private Integer quizzesAprobados;

  @Column(name = "total_quizzes")
  private Integer totalQuizzes;

  public Certificado() {}

  public Certificado(Usuario usuario, Curso curso, String codigoVerificacion) {
    this.usuario = usuario;
    this.curso = curso;
    this.codigoVerificacion = codigoVerificacion;
    this.fechaEmision = LocalDateTime.now();
  }
}
