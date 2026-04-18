package com.cajica.stream.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(
    name = "contenido_progreso",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "tipo", "contenido_id"}))
public class ContenidoProgreso {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "usuario_id", nullable = false)
  private Long usuarioId;

  @Column(name = "curso_id", nullable = false)
  private Long cursoId;

  @Column(name = "tipo", nullable = false, length = 10)
  private String tipo;

  @Column(name = "contenido_id", nullable = false)
  private Long contenidoId;

  @Column(name = "fecha_completado")
  private LocalDateTime fechaCompletado;

  public ContenidoProgreso() {}

  public ContenidoProgreso(Long usuarioId, Long cursoId, String tipo, Long contenidoId) {
    this.usuarioId = usuarioId;
    this.cursoId = cursoId;
    this.tipo = tipo;
    this.contenidoId = contenidoId;
    this.fechaCompletado = LocalDateTime.now();
  }
}
