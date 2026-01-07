package com.cajica.stream.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "video_respuesta")
public class VideoRespuesta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pregunta_id", nullable = false)
  private VideoPregunta pregunta;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String contenido;

  @Column(name = "es_instructor", nullable = false)
  private boolean esInstructor = false;

  @Column(name = "es_aceptada", nullable = false)
  private boolean esAceptada = false;

  @Column(name = "visible", nullable = false)
  private boolean visible = true;

  @Column(name = "fecha_creacion", insertable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private LocalDateTime updatedAt;
}
