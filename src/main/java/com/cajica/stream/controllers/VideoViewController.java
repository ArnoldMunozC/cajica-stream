package com.cajica.stream.controllers;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.Video;
import com.cajica.stream.services.CursoService;
import com.cajica.stream.services.VideoService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cursos/{cursoId}/videos")
public class VideoViewController {

  private final VideoService videoService;
  private final CursoService cursoService;

  @Autowired
  public VideoViewController(VideoService videoService, CursoService cursoService) {
    this.videoService = videoService;
    this.cursoService = cursoService;
  }

  @GetMapping
  public String listarVideos(@PathVariable Long cursoId, Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    if (curso.isPresent()) {
      List<Video> videos = videoService.findByCursoId(cursoId);
      model.addAttribute("curso", curso.get());
      model.addAttribute("videos", videos);
      return "videos/lista";
    } else {
      return "redirect:/cursos";
    }
  }

  @GetMapping("/{id}")
  public String verVideo(@PathVariable Long cursoId, @PathVariable Long id, Model model) {
    // Redirigir primero al controlador de seguridad para verificar acceso
    return "redirect:/cursos/" + cursoId + "/videos/" + id + "/secure";
  }

  @GetMapping("/{id}/view")
  public String mostrarVideo(@PathVariable Long cursoId, @PathVariable Long id, Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    Optional<Video> video = videoService.findById(id);

    if (curso.isPresent() && video.isPresent()) {
      model.addAttribute("curso", curso.get());
      model.addAttribute("video", video.get());

      // Obtener videos relacionados del mismo curso
      List<Video> videosRelacionados = videoService.findByCursoId(cursoId);
      model.addAttribute("videosRelacionados", videosRelacionados);

      return "videos/reproducir";
    } else {
      return "redirect:/cursos/" + cursoId;
    }
  }

  @GetMapping("/nuevo")
  @PreAuthorize("hasRole('ADMIN')")
  public String mostrarFormularioNuevo(@PathVariable Long cursoId, Model model) {
    Optional<Curso> curso = cursoService.findById(cursoId);
    if (curso.isPresent()) {
      model.addAttribute("curso", curso.get());
      model.addAttribute("video", new Video());
      model.addAttribute("accion", "crear");
      return "videos/formulario";
    } else {
      return "redirect:/cursos";
    }
  }

  @PostMapping("/nuevo")
  @PreAuthorize("hasRole('ADMIN')")
  public String crearVideo(
      @PathVariable Long cursoId,
      @ModelAttribute Video video,
      @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
      @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
      RedirectAttributes redirectAttributes) {

    // Validar que al menos uno de los dos campos esté presente: videoUrl o videoFile
    if ((video.getVideoUrl() == null || video.getVideoUrl().isEmpty())
        && (videoFile == null || videoFile.isEmpty())) {
      redirectAttributes.addFlashAttribute(
          "error", "Debes proporcionar una URL de video o subir un archivo de video");
      return "redirect:/cursos/" + cursoId + "/videos/nuevo";
    }

    Video nuevoVideo = videoService.addVideoToCurso(cursoId, video, thumbnail, videoFile);

    if (nuevoVideo != null) {
      redirectAttributes.addFlashAttribute("mensaje", "Video creado exitosamente");
      return "redirect:/cursos/" + cursoId + "/videos/" + nuevoVideo.getId();
    } else {
      return "redirect:/cursos/" + cursoId;
    }
  }

  @GetMapping("/{id}/editar")
  @PreAuthorize("hasRole('ADMIN')")
  public String mostrarFormularioEditar(
      @PathVariable Long cursoId, @PathVariable Long id, Model model) {

    Optional<Curso> curso = cursoService.findById(cursoId);
    Optional<Video> video = videoService.findById(id);

    if (curso.isPresent() && video.isPresent()) {
      model.addAttribute("curso", curso.get());
      model.addAttribute("video", video.get());
      model.addAttribute("accion", "editar");
      return "videos/formulario";
    } else {
      return "redirect:/cursos/" + cursoId;
    }
  }

  @PostMapping("/{id}/editar")
  @PreAuthorize("hasRole('ADMIN')")
  public String actualizarVideo(
      @PathVariable Long cursoId,
      @PathVariable Long id,
      @ModelAttribute Video video,
      @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
      @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
      RedirectAttributes redirectAttributes) {

    // Validar que al menos uno de los dos campos esté presente: videoUrl o videoFile
    // Si no hay videoUrl y no hay un archivo nuevo, verificar si ya existe un videoFilePath
    Optional<Video> existingVideo = videoService.findById(id);
    boolean hasExistingVideoFile =
        existingVideo.isPresent()
            && existingVideo.get().getVideoFilePath() != null
            && !existingVideo.get().getVideoFilePath().isEmpty();

    if ((video.getVideoUrl() == null || video.getVideoUrl().isEmpty())
        && (videoFile == null || videoFile.isEmpty())
        && !hasExistingVideoFile) {
      redirectAttributes.addFlashAttribute(
          "error", "Debes proporcionar una URL de video o subir un archivo de video");
      return "redirect:/cursos/" + cursoId + "/videos/" + id + "/editar";
    }

    Video videoActualizado = videoService.update(id, video, thumbnail, videoFile);

    if (videoActualizado != null) {
      redirectAttributes.addFlashAttribute("mensaje", "Video actualizado exitosamente");
      return "redirect:/cursos/" + cursoId + "/videos/" + videoActualizado.getId();
    } else {
      return "redirect:/cursos/" + cursoId;
    }
  }

  @GetMapping("/{id}/eliminar")
  @PreAuthorize("hasRole('ADMIN')")
  public String eliminarVideo(
      @PathVariable Long cursoId, @PathVariable Long id, RedirectAttributes redirectAttributes) {

    Optional<Video> video = videoService.findById(id);
    if (video.isPresent()) {
      videoService.deleteById(id);
      redirectAttributes.addFlashAttribute("mensaje", "Video eliminado exitosamente");
    }

    return "redirect:/cursos/" + cursoId + "/videos";
  }
}
