package com.cajica.stream.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "quiz_respuesta")
public class QuizRespuesta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "intento_id", nullable = false)
  private QuizIntento intento;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pregunta_id", nullable = false)
  private QuizPregunta pregunta;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opcion_id")
  private QuizOpcion opcion;

  @Column(nullable = false)
  private boolean correcta;
}
