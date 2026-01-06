package com.cajica.stream.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "quiz_intento")
public class QuizIntento {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "quiz_id", nullable = false)
  private Quiz quiz;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(name = "numero_intento", nullable = false)
  private Integer numeroIntento;

  @Column(nullable = false)
  private Integer puntaje;

  @Column(name = "total_preguntas", nullable = false)
  private Integer totalPreguntas;

  @Column(nullable = false)
  private boolean aprobado;

  @Column(name = "fecha_creacion", nullable = false)
  private LocalDateTime fechaCreacion;

  @OneToMany(mappedBy = "intento", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<QuizRespuesta> respuestas = new ArrayList<>();

  @PrePersist
  void prePersist() {
    if (fechaCreacion == null) {
      fechaCreacion = LocalDateTime.now();
    }
  }
}
