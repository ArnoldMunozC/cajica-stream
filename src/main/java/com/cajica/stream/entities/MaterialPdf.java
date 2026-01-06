package com.cajica.stream.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class MaterialPdf {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String titulo;

  @Column(name = "seccion", length = 100)
  private String seccion;

  @Column(name = "pdf_file_path", nullable = false)
  private String pdfFilePath;

  @Column(name = "orden")
  private Integer orden;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "curso_id")
  private Curso curso;
}
