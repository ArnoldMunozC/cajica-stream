package com.cajica.stream.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "configuracion_sistema")
public class ConfiguracionSistema {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "clave", nullable = false, unique = true, length = 100)
  private String clave;

  @Lob
  @Column(name = "valor", columnDefinition = "TEXT")
  private String valor;

  @Column(name = "descripcion", length = 300)
  private String descripcion;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public ConfiguracionSistema() {}
}
