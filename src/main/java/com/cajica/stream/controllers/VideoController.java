package com.cajica.stream.controllers;

import com.cajica.stream.entities.Video;
import com.cajica.stream.services.VideoService;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoController {

  private final VideoService videoService;

  @Autowired
  public VideoController(VideoService videoService) {
    this.videoService = videoService;
  }

  @GetMapping
  public ResponseEntity<List<Video>> getAllVideos() {
    List<Video> videos = videoService.findAll();
    return ResponseEntity.ok(videos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
    Optional<Video> video = videoService.findById(id);
    return video.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/curso/{cursoId}")
  public ResponseEntity<List<Video>> getVideosByCursoId(@PathVariable Long cursoId) {
    List<Video> videos = videoService.findByCursoId(cursoId);
    return ResponseEntity.ok(videos);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Video> createVideo(@RequestBody Video video) {
    Video nuevoVideo = videoService.save(video);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(nuevoVideo.getId())
            .toUri();
    return ResponseEntity.created(location).body(nuevoVideo);
  }

  @PostMapping("/with-thumbnail")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Video> createVideoWithThumbnail(
      @RequestParam("titulo") String titulo,
      @RequestParam("descripcion") String descripcion,
      @RequestParam("videoUrl") String videoUrl,
      @RequestParam(value = "duracion", required = false) Integer duracion,
      @RequestParam("cursoId") Long cursoId,
      @RequestParam(value = "orden", required = false) Integer orden,
      @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) {

    Video video = new Video();
    video.setTitulo(titulo);
    video.setDescripcion(descripcion);
    video.setVideoUrl(videoUrl);
    video.setDuracion(duracion);
    video.setOrden(orden);

    Video nuevoVideo = videoService.addVideoToCurso(cursoId, video, thumbnail);

    if (nuevoVideo != null) {
      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .path("/{id}")
              .buildAndExpand(nuevoVideo.getId())
              .toUri();
      return ResponseEntity.created(location).body(nuevoVideo);
    } else {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Video> updateVideo(@PathVariable Long id, @RequestBody Video video) {
    Video videoActualizado = videoService.update(id, video);
    return videoActualizado != null
        ? ResponseEntity.ok(videoActualizado)
        : ResponseEntity.notFound().build();
  }

  @PutMapping("/{id}/with-thumbnail")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Video> updateVideoWithThumbnail(
      @PathVariable Long id,
      @RequestParam("titulo") String titulo,
      @RequestParam("descripcion") String descripcion,
      @RequestParam("videoUrl") String videoUrl,
      @RequestParam(value = "duracion", required = false) Integer duracion,
      @RequestParam(value = "orden", required = false) Integer orden,
      @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) {

    Video video = new Video();
    video.setTitulo(titulo);
    video.setDescripcion(descripcion);
    video.setVideoUrl(videoUrl);
    video.setDuracion(duracion);
    video.setOrden(orden);

    Video videoActualizado = videoService.update(id, video, thumbnail);

    return videoActualizado != null
        ? ResponseEntity.ok(videoActualizado)
        : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
    Optional<Video> video = videoService.findById(id);
    if (video.isPresent()) {
      videoService.deleteById(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping("/reorder")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> reorderVideos(
      @RequestParam("cursoId") Long cursoId, @RequestBody List<Long> videoIds) {
    videoService.reorderVideos(cursoId, videoIds);
    return ResponseEntity.ok().build();
  }
}
