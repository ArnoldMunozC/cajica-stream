package com.cajica.stream.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "video_progreso")
public class VideoProgreso {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "curso_id", nullable = false)
  private Curso curso;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "video_id", nullable = false)
  private Video video;

  @Column(name = "segundo", nullable = false)
  private Double segundo;

  @Column(name = "completado", nullable = false)
  private boolean completado = false;

  @Column(name = "fecha_completado")
  private LocalDateTime fechaCompletado;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private LocalDateTime updatedAt;
}
