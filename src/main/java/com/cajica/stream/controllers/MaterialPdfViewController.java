package com.cajica.stream.controllers;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.MaterialPdf;
import com.cajica.stream.services.CursoService;
import com.cajica.stream.services.MaterialPdfService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cursos/{cursoId}/pdfs")
public class MaterialPdfViewController {

  private static List<String> buildSeccionOptions() {
    List<String> opts = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      opts.add("Módulo " + i);
    }
    return opts;
  }

  private final CursoService cursoService;
  private final MaterialPdfService materialPdfService;

  public MaterialPdfViewController(
      CursoService cursoService, MaterialPdfService materialPdfService) {
    this.cursoService = cursoService;
    this.materialPdfService = materialPdfService;
  }

  @GetMapping("/nuevo")
  @PreAuthorize("hasRole('ADMIN')")
  public String mostrarFormularioNuevo(@PathVariable Long cursoId, Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    if (curso.isEmpty()) {
      return "redirect:/cursos";
    }

    model.addAttribute("curso", curso.get());
    model.addAttribute("materialPdf", new MaterialPdf());
    model.addAttribute("seccionOptions", buildSeccionOptions());
    return "pdfs/formulario";
  }

  @PostMapping("/nuevo")
  @PreAuthorize("hasRole('ADMIN')")
  public String crearPdf(
      @PathVariable Long cursoId,
      @RequestParam("titulo") String titulo,
      @RequestParam(value = "seccion", required = false) String seccion,
      @RequestParam(value = "orden", required = false) Integer orden,
      @RequestParam("pdfFile") MultipartFile pdfFile,
      RedirectAttributes redirectAttributes) {
    try {
      MaterialPdf materialPdf = new MaterialPdf();
      materialPdf.setTitulo(titulo);
      materialPdf.setSeccion(seccion);
      materialPdf.setOrden(orden);

      MaterialPdf created = materialPdfService.addPdfToCurso(cursoId, materialPdf, pdfFile);
      if (created == null) {
        redirectAttributes.addFlashAttribute("error", "Curso no encontrado");
        return "redirect:/cursos";
      }

      redirectAttributes.addFlashAttribute("mensaje", "PDF subido exitosamente");
      return "redirect:/cursos/" + cursoId + "/videos";
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/cursos/" + cursoId + "/pdfs/nuevo";
    }
  }

  @GetMapping("/{pdfId}/editar")
  @PreAuthorize("hasRole('ADMIN')")
  public String mostrarFormularioEditar(
      @PathVariable Long cursoId, @PathVariable Long pdfId, Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    Optional<MaterialPdf> pdf = materialPdfService.findById(pdfId);
    if (curso.isEmpty() || pdf.isEmpty()) {
      return "redirect:/cursos/" + cursoId;
    }

    model.addAttribute("curso", curso.get());
    model.addAttribute("materialPdf", pdf.get());
    model.addAttribute("editando", true);
    model.addAttribute("seccionOptions", buildSeccionOptions());
    return "pdfs/formulario";
  }

  @PostMapping("/{pdfId}/editar")
  @PreAuthorize("hasRole('ADMIN')")
  public String editarPdf(
      @PathVariable Long cursoId,
      @PathVariable Long pdfId,
      @RequestParam("titulo") String titulo,
      @RequestParam(value = "seccion", required = false) String seccion,
      @RequestParam(value = "orden", required = false) Integer orden,
      RedirectAttributes redirectAttributes) {

    MaterialPdf updated = materialPdfService.update(pdfId, titulo, seccion, orden);
    if (updated == null) {
      redirectAttributes.addFlashAttribute("error", "PDF no encontrado");
      return "redirect:/cursos/" + cursoId;
    }

    redirectAttributes.addFlashAttribute("mensaje", "PDF actualizado exitosamente");
    return "redirect:/cursos/" + cursoId;
  }

  @GetMapping("/{pdfId}/eliminar")
  @PreAuthorize("hasRole('ADMIN')")
  public String eliminarPdf(
      @PathVariable Long cursoId,
      @PathVariable Long pdfId,
      RedirectAttributes redirectAttributes) {

    boolean deleted = materialPdfService.delete(pdfId);
    if (deleted) {
      redirectAttributes.addFlashAttribute("mensaje", "PDF eliminado exitosamente");
    } else {
      redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el PDF");
    }

    return "redirect:/cursos/" + cursoId;
  }
}
