package com.cajica.stream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO para transferir información básica de cursos sin cargar relaciones complejas. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoDTO {
  private Long id;
  private String nombre;
  private String descripcion;
  private String categoria;
  private String imagenPath;
}
