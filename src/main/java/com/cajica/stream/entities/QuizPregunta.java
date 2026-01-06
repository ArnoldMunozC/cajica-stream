package com.cajica.stream.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "quiz_pregunta")
public class QuizPregunta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "quiz_id", nullable = false)
  private Quiz quiz;

  @Column(nullable = false, length = 1000)
  private String enunciado;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private QuizPreguntaTipo tipo;

  @Column(name = "orden")
  private Integer orden;

  @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("orden ASC, id ASC")
  private List<QuizOpcion> opciones = new ArrayList<>();
}
