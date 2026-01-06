package com.cajica.stream.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "quiz")
public class Quiz {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String titulo;

  @Column(name = "seccion", length = 100)
  private String seccion;

  @Column(name = "max_intentos", nullable = false)
  private Integer maxIntentos = 1;

  @Column(name = "activo", nullable = false)
  private boolean activo = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "curso_id", nullable = false)
  private Curso curso;

  @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("orden ASC, id ASC")
  private List<QuizPregunta> preguntas = new ArrayList<>();
}
