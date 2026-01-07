package com.cajica.stream.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "video_pregunta")
public class VideoPregunta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "curso_id", nullable = false)
  private Curso curso;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "video_id", nullable = false)
  private Video video;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(nullable = false)
  private String titulo;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String contenido;

  @Column(name = "cerrada", nullable = false)
  private boolean cerrada = false;

  @Column(name = "fecha_creacion", insertable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("fechaCreacion ASC, id ASC")
  private List<VideoRespuesta> respuestas = new ArrayList<>();
}
