package com.cajica.stream.controllers;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.services.CursoService;
import com.cajica.stream.services.FileStorageService;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "*")
public class CursoController {

  private final CursoService cursoService;
  private final FileStorageService fileStorageService;

  @Autowired
  public CursoController(CursoService cursoService, FileStorageService fileStorageService) {
    this.cursoService = cursoService;
    this.fileStorageService = fileStorageService;
  }

  @GetMapping
  public ResponseEntity<List<Curso>> getAllCursos() {
    List<Curso> cursos = cursoService.findAll();
    return ResponseEntity.ok(cursos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Curso> getCursoById(@PathVariable Long id) {
    Optional<Curso> curso = cursoService.findById(id);
    return curso.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Curso> createCurso(@RequestBody Curso curso) {
    Curso nuevoCurso = cursoService.save(curso);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(nuevoCurso.getId())
            .toUri();
    return ResponseEntity.created(location).body(nuevoCurso);
  }

  @PostMapping("/with-image")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Curso> createCursoWithImage(
      @RequestParam("nombre") String nombre,
      @RequestParam("descripcion") String descripcion,
      @RequestParam("categoria") String categoria,
      @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

    Curso curso = new Curso();
    curso.setNombre(nombre);
    curso.setDescripcion(descripcion);
    curso.setCategoria(categoria);

    Curso nuevoCurso = cursoService.save(curso, imagen);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(nuevoCurso.getId())
            .toUri();

    return ResponseEntity.created(location).body(nuevoCurso);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Curso> updateCurso(@PathVariable Long id, @RequestBody Curso curso) {
    Curso cursoActualizado = cursoService.update(id, curso);
    return cursoActualizado != null
        ? ResponseEntity.ok(cursoActualizado)
        : ResponseEntity.notFound().build();
  }

  @PutMapping("/{id}/with-image")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Curso> updateCursoWithImage(
      @PathVariable Long id,
      @RequestParam("nombre") String nombre,
      @RequestParam("descripcion") String descripcion,
      @RequestParam("categoria") String categoria,
      @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

    Curso curso = new Curso();
    curso.setNombre(nombre);
    curso.setDescripcion(descripcion);
    curso.setCategoria(categoria);

    Curso cursoActualizado = cursoService.update(id, curso, imagen);

    return cursoActualizado != null
        ? ResponseEntity.ok(cursoActualizado)
        : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteCurso(@PathVariable Long id) {
    Optional<Curso> curso = cursoService.findById(id);
    if (curso.isPresent()) {
      cursoService.deleteById(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/images/{fileName:.+}")
  public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
    try {
      Path filePath = fileStorageService.getFilePath(fileName);
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists()) {
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG) // Puedes ajustar según el tipo de imagen
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/videos/{fileName:.+}")
  public ResponseEntity<Resource> getVideo(@PathVariable String fileName) {
    try {
      Path filePath = fileStorageService.getFilePath(fileName, true);
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists()) {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("video/mp4")) // Ajustamos al tipo de video
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/pdfs/{fileName:.+}")
  public ResponseEntity<Resource> getPdf(@PathVariable String fileName) {
    try {
      Path filePath = fileStorageService.getFilePath(fileName);
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists()) {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
