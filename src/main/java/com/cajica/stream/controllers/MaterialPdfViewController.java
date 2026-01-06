package com.cajica.stream.controllers;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.MaterialPdf;
import com.cajica.stream.services.CursoService;
import com.cajica.stream.services.MaterialPdfService;
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
}
