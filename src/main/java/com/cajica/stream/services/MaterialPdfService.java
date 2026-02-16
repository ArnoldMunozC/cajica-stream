package com.cajica.stream.services;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.MaterialPdf;
import com.cajica.stream.repositories.CursoRepository;
import com.cajica.stream.repositories.MaterialPdfRepository;
import com.cajica.stream.repositories.VideoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MaterialPdfService {

  private final MaterialPdfRepository materialPdfRepository;
  private final CursoRepository cursoRepository;
  private final FileStorageService fileStorageService;
  private final VideoRepository videoRepository;

  public MaterialPdfService(
      MaterialPdfRepository materialPdfRepository,
      CursoRepository cursoRepository,
      FileStorageService fileStorageService,
      VideoRepository videoRepository) {
    this.materialPdfRepository = materialPdfRepository;
    this.cursoRepository = cursoRepository;
    this.fileStorageService = fileStorageService;
    this.videoRepository = videoRepository;
  }

  public List<MaterialPdf> findByCursoId(Long cursoId) {
    return materialPdfRepository.findByCursoIdOrderByOrdenAscIdAsc(cursoId);
  }

  public Optional<MaterialPdf> findById(Long id) {
    return materialPdfRepository.findById(id);
  }

  public MaterialPdf addPdfToCurso(Long cursoId, MaterialPdf materialPdf, MultipartFile pdfFile) {
    Optional<Curso> cursoOpt = cursoRepository.findById(cursoId);
    if (cursoOpt.isEmpty()) {
      return null;
    }

    if (pdfFile == null || pdfFile.isEmpty()) {
      throw new IllegalArgumentException("Debes seleccionar un archivo PDF");
    }

    String storedFileName = fileStorageService.storeFile(pdfFile);

    materialPdf.setCurso(cursoOpt.get());
    materialPdf.setPdfFilePath(storedFileName);

    String seccionNormalizada = null;
    if (materialPdf.getSeccion() != null && !materialPdf.getSeccion().isBlank()) {
      seccionNormalizada = materialPdf.getSeccion().trim();
      materialPdf.setSeccion(seccionNormalizada);
    } else {
      materialPdf.setSeccion(null);
    }

    if (materialPdf.getOrden() != null) {
      List<com.cajica.stream.entities.Video> videosParaDesplazar =
          videoRepository.findByCursoIdAndSeccionWithOrdenGteOrderByOrdenDescIdDesc(
              cursoId, seccionNormalizada, materialPdf.getOrden());

      for (com.cajica.stream.entities.Video existente : videosParaDesplazar) {
        existente.setOrden(existente.getOrden() + 1);
      }
      videoRepository.saveAll(videosParaDesplazar);

      List<MaterialPdf> paraDesplazar =
          materialPdfRepository.findByCursoIdAndSeccionWithOrdenGteOrderByOrdenDescIdDesc(
              cursoId, seccionNormalizada, materialPdf.getOrden());

      for (MaterialPdf existente : paraDesplazar) {
        existente.setOrden(existente.getOrden() + 1);
      }
      materialPdfRepository.saveAll(paraDesplazar);
    }

    if (materialPdf.getOrden() == null) {
      Integer maxVideoOrden =
          videoRepository.findMaxOrdenByCursoIdAndSeccion(cursoId, seccionNormalizada);
      Integer maxPdfOrden =
          materialPdfRepository.findMaxOrdenByCursoIdAndSeccion(cursoId, seccionNormalizada);

      int max = 0;
      if (maxVideoOrden != null && maxVideoOrden > max) {
        max = maxVideoOrden;
      }
      if (maxPdfOrden != null && maxPdfOrden > max) {
        max = maxPdfOrden;
      }

      materialPdf.setOrden(max + 1);
    }

    return materialPdfRepository.save(materialPdf);
  }

  public MaterialPdf update(Long id, String titulo, String seccion, Integer orden) {
    Optional<MaterialPdf> pdfOpt = materialPdfRepository.findById(id);
    if (pdfOpt.isEmpty()) {
      return null;
    }

    MaterialPdf pdf = pdfOpt.get();
    pdf.setTitulo(titulo);

    String seccionNormalizada = null;
    if (seccion != null && !seccion.isBlank()) {
      seccionNormalizada = seccion.trim();
    }
    pdf.setSeccion(seccionNormalizada);
    pdf.setOrden(orden);

    return materialPdfRepository.save(pdf);
  }

  public boolean delete(Long id) {
    Optional<MaterialPdf> pdfOpt = materialPdfRepository.findById(id);
    if (pdfOpt.isEmpty()) {
      return false;
    }

    MaterialPdf pdf = pdfOpt.get();
    if (pdf.getPdfFilePath() != null) {
      fileStorageService.deleteFile(pdf.getPdfFilePath());
    }

    materialPdfRepository.deleteById(id);
    return true;
  }
}
