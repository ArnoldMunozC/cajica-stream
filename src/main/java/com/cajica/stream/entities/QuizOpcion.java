package com.cajica.stream.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "quiz_opcion")
public class QuizOpcion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pregunta_id", nullable = false)
  private QuizPregunta pregunta;

  @Column(nullable = false, length = 1000)
  private String texto;

  @Column(nullable = false)
  private boolean correcta = false;

  @Column(name = "orden")
  private Integer orden;
}
