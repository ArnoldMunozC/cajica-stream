package com.cajica.stream.services;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.repositories.CursoRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CursoService {

  private final CursoRepository cursoRepository;
  private final FileStorageService fileStorageService;

  @Autowired
  public CursoService(CursoRepository cursoRepository, FileStorageService fileStorageService) {
    this.cursoRepository = cursoRepository;
    this.fileStorageService = fileStorageService;
  }

  public List<Curso> findAll() {
    // Por defecto, solo devuelve cursos activos
    return cursoRepository.findAll().stream().filter(Curso::isActivo).collect(Collectors.toList());
  }

  public List<Curso> findAllIncludingDisabled() {
    // Devuelve todos los cursos, incluyendo los deshabilitados
    return cursoRepository.findAll();
  }

  public Optional<Curso> findById(Long id) {
    return cursoRepository.findById(id);
  }

  public Curso save(Curso curso) {
    return cursoRepository.save(curso);
  }

  public Curso save(Curso curso, MultipartFile imagen) {
    if (imagen != null && !imagen.isEmpty()) {
      String imagenPath = fileStorageService.storeFile(imagen);
      curso.setImagenPath(imagenPath);
    }
    return cursoRepository.save(curso);
  }

  /** Método obsoleto que será eliminado en futuras versiones. Usar disableCurso en su lugar. */
  @Deprecated
  public void deleteById(Long id) {
    try {
      cursoRepository.deleteById(id);
    } catch (DataIntegrityViolationException e) {
      // Si hay una violación de integridad, deshabilitamos el curso en lugar de eliminarlo
      disableCurso(id);
    }
  }

  /**
   * Deshabilita un curso en lugar de eliminarlo físicamente.
   *
   * @param id ID del curso a deshabilitar
   * @return true si el curso fue deshabilitado exitosamente, false si no se encontró
   */
  public boolean disableCurso(Long id) {
    Optional<Curso> optionalCurso = cursoRepository.findById(id);
    if (optionalCurso.isPresent()) {
      Curso curso = optionalCurso.get();
      curso.setActivo(false);
      cursoRepository.save(curso);
      return true;
    }
    return false;
  }

  /**
   * Habilita un curso previamente deshabilitado.
   *
   * @param id ID del curso a habilitar
   * @return true si el curso fue habilitado exitosamente, false si no se encontró
   */
  public boolean enableCurso(Long id) {
    Optional<Curso> optionalCurso = cursoRepository.findById(id);
    if (optionalCurso.isPresent()) {
      Curso curso = optionalCurso.get();
      curso.setActivo(true);
      cursoRepository.save(curso);
      return true;
    }
    return false;
  }

  public Curso update(Long id, Curso cursoDetails) {
    Optional<Curso> optionalCurso = cursoRepository.findById(id);
    if (optionalCurso.isPresent()) {
      Curso curso = optionalCurso.get();
      curso.setNombre(cursoDetails.getNombre());
      curso.setDescripcion(cursoDetails.getDescripcion());
      curso.setCategoria(cursoDetails.getCategoria());

      // Solo actualizar la ruta de la imagen si se proporciona una nueva
      if (cursoDetails.getImagenPath() != null) {
        curso.setImagenPath(cursoDetails.getImagenPath());
      }

      return cursoRepository.save(curso);
    }
    return null;
  }

  public Curso update(Long id, Curso cursoDetails, MultipartFile imagen) {
    Optional<Curso> optionalCurso = cursoRepository.findById(id);
    if (optionalCurso.isPresent()) {
      Curso curso = optionalCurso.get();
      curso.setNombre(cursoDetails.getNombre());
      curso.setDescripcion(cursoDetails.getDescripcion());
      curso.setCategoria(cursoDetails.getCategoria());

      // Actualizar la imagen si se proporciona una nueva
      if (imagen != null && !imagen.isEmpty()) {
        String imagenPath = fileStorageService.storeFile(imagen);
        curso.setImagenPath(imagenPath);
      }

      return cursoRepository.save(curso);
    }
    return null;
  }
}
