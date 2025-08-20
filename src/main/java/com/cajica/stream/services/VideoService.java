package com.cajica.stream.services;

import com.cajica.stream.entities.Curso;
import com.cajica.stream.entities.Video;
import com.cajica.stream.repositories.CursoRepository;
import com.cajica.stream.repositories.VideoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoService {

  private final VideoRepository videoRepository;
  private final CursoRepository cursoRepository;
  private final FileStorageService fileStorageService;

  @Autowired
  public VideoService(
      VideoRepository videoRepository,
      CursoRepository cursoRepository,
      FileStorageService fileStorageService) {
    this.videoRepository = videoRepository;
    this.cursoRepository = cursoRepository;
    this.fileStorageService = fileStorageService;
  }

  public List<Video> findAll() {
    return videoRepository.findAll();
  }

  public Optional<Video> findById(Long id) {
    return videoRepository.findById(id);
  }

  public List<Video> findByCursoId(Long cursoId) {
    return videoRepository.findByCursoIdOrderByOrdenAsc(cursoId);
  }

  public Video save(Video video) {
    // Si no se especifica un orden, asignar el siguiente número
    if (video.getOrden() == null) {
      Long count = videoRepository.countByCursoId(video.getCurso().getId());
      video.setOrden(count.intValue() + 1);
    }
    return videoRepository.save(video);
  }

  public Video save(Video video, MultipartFile thumbnail) {
    // Guardar la miniatura si se proporciona
    if (thumbnail != null && !thumbnail.isEmpty()) {
      String thumbnailPath = fileStorageService.storeFile(thumbnail);
      video.setThumbnailPath(thumbnailPath);
    }

    // Si no se especifica un orden, asignar el siguiente número
    if (video.getOrden() == null) {
      Long count = videoRepository.countByCursoId(video.getCurso().getId());
      video.setOrden(count.intValue() + 1);
    }

    return videoRepository.save(video);
  }

  public Video save(Video video, MultipartFile thumbnail, MultipartFile videoFile) {
    // Guardar la miniatura si se proporciona
    if (thumbnail != null && !thumbnail.isEmpty()) {
      String thumbnailPath = fileStorageService.storeFile(thumbnail);
      video.setThumbnailPath(thumbnailPath);
    }

    // Guardar el archivo de video si se proporciona
    if (videoFile != null && !videoFile.isEmpty()) {
      String videoFilePath = fileStorageService.storeFile(videoFile, true);
      video.setVideoFilePath(videoFilePath);
    }

    // Si no se especifica un orden, asignar el siguiente número
    if (video.getOrden() == null) {
      Long count = videoRepository.countByCursoId(video.getCurso().getId());
      video.setOrden(count.intValue() + 1);
    }

    return videoRepository.save(video);
  }

  public Video addVideoToCurso(Long cursoId, Video video, MultipartFile thumbnail) {
    Optional<Curso> cursoOpt = cursoRepository.findById(cursoId);
    if (cursoOpt.isPresent()) {
      Curso curso = cursoOpt.get();
      video.setCurso(curso);

      // Guardar la miniatura si se proporciona
      if (thumbnail != null && !thumbnail.isEmpty()) {
        String thumbnailPath = fileStorageService.storeFile(thumbnail);
        video.setThumbnailPath(thumbnailPath);
      }

      // Si no se especifica un orden, asignar el siguiente número
      if (video.getOrden() == null) {
        Long count = videoRepository.countByCursoId(cursoId);
        video.setOrden(count.intValue() + 1);
      }

      return videoRepository.save(video);
    }
    return null;
  }

  public Video addVideoToCurso(
      Long cursoId, Video video, MultipartFile thumbnail, MultipartFile videoFile) {
    Optional<Curso> cursoOpt = cursoRepository.findById(cursoId);
    if (cursoOpt.isPresent()) {
      Curso curso = cursoOpt.get();
      video.setCurso(curso);

      // Guardar la miniatura si se proporciona
      if (thumbnail != null && !thumbnail.isEmpty()) {
        String thumbnailPath = fileStorageService.storeFile(thumbnail);
        video.setThumbnailPath(thumbnailPath);
      }

      // Guardar el archivo de video si se proporciona
      if (videoFile != null && !videoFile.isEmpty()) {
        String videoFilePath = fileStorageService.storeFile(videoFile, true);
        video.setVideoFilePath(videoFilePath);
      }

      // Si no se especifica un orden, asignar el siguiente número
      if (video.getOrden() == null) {
        Long count = videoRepository.countByCursoId(cursoId);
        video.setOrden(count.intValue() + 1);
      }

      return videoRepository.save(video);
    }
    return null;
  }

  public void deleteById(Long id) {
    videoRepository.deleteById(id);
  }

  public Video update(Long id, Video videoDetails) {
    Optional<Video> optionalVideo = videoRepository.findById(id);
    if (optionalVideo.isPresent()) {
      Video video = optionalVideo.get();
      video.setTitulo(videoDetails.getTitulo());
      video.setDescripcion(videoDetails.getDescripcion());
      video.setVideoUrl(videoDetails.getVideoUrl());
      video.setDuracion(videoDetails.getDuracion());
      video.setOrden(videoDetails.getOrden());

      // Solo actualizar la miniatura si se proporciona una nueva
      if (videoDetails.getThumbnailPath() != null) {
        video.setThumbnailPath(videoDetails.getThumbnailPath());
      }

      return videoRepository.save(video);
    }
    return null;
  }

  public Video update(Long id, Video videoDetails, MultipartFile thumbnail) {
    Optional<Video> optionalVideo = videoRepository.findById(id);
    if (optionalVideo.isPresent()) {
      Video video = optionalVideo.get();
      video.setTitulo(videoDetails.getTitulo());
      video.setDescripcion(videoDetails.getDescripcion());
      video.setVideoUrl(videoDetails.getVideoUrl());
      video.setDuracion(videoDetails.getDuracion());
      video.setOrden(videoDetails.getOrden());

      // Actualizar la miniatura si se proporciona una nueva
      if (thumbnail != null && !thumbnail.isEmpty()) {
        String thumbnailPath = fileStorageService.storeFile(thumbnail);
        video.setThumbnailPath(thumbnailPath);
      }

      return videoRepository.save(video);
    }
    return null;
  }

  public Video update(
      Long id, Video videoDetails, MultipartFile thumbnail, MultipartFile videoFile) {
    Optional<Video> optionalVideo = videoRepository.findById(id);
    if (optionalVideo.isPresent()) {
      Video video = optionalVideo.get();
      video.setTitulo(videoDetails.getTitulo());
      video.setDescripcion(videoDetails.getDescripcion());
      video.setVideoUrl(videoDetails.getVideoUrl());
      video.setDuracion(videoDetails.getDuracion());
      video.setOrden(videoDetails.getOrden());

      // Actualizar la miniatura si se proporciona una nueva
      if (thumbnail != null && !thumbnail.isEmpty()) {
        String thumbnailPath = fileStorageService.storeFile(thumbnail);
        video.setThumbnailPath(thumbnailPath);
      }

      // Actualizar el archivo de video si se proporciona uno nuevo
      if (videoFile != null && !videoFile.isEmpty()) {
        String videoFilePath = fileStorageService.storeFile(videoFile, true);
        video.setVideoFilePath(videoFilePath);
      }

      return videoRepository.save(video);
    }
    return null;
  }

  public void reorderVideos(Long cursoId, List<Long> videoIds) {
    for (int i = 0; i < videoIds.size(); i++) {
      Optional<Video> videoOpt = videoRepository.findById(videoIds.get(i));
      if (videoOpt.isPresent()) {
        Video video = videoOpt.get();
        video.setOrden(i + 1);
        videoRepository.save(video);
      }
    }
  }
}
