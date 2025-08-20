package com.cajica.stream.services;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

  @Value("${app.upload.dir}")
  private String uploadDir;

  @Value("${app.upload.video.dir}")
  private String videoUploadDir;

  private Path fileStoragePath;
  private Path videoStoragePath;

  @PostConstruct
  public void init() {
    try {
      // Inicializar directorio para imágenes
      fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
      Files.createDirectories(fileStoragePath);
      System.out.println(
          "Directorio de almacenamiento de imágenes inicializado en: " + fileStoragePath);

      // Inicializar directorio para videos
      videoStoragePath = Paths.get(videoUploadDir).toAbsolutePath().normalize();
      Files.createDirectories(videoStoragePath);
      System.out.println(
          "Directorio de almacenamiento de videos inicializado en: " + videoStoragePath);
    } catch (IOException e) {
      throw new RuntimeException("No se pudo crear el directorio de almacenamiento de archivos", e);
    }
  }

  public String storeFile(MultipartFile file) {
    return storeFile(file, false);
  }

  public String storeFile(MultipartFile file, boolean isVideo) {
    try {
      // Verificar que el archivo no esté vacío
      if (file.isEmpty()) {
        throw new RuntimeException("No se puede almacenar un archivo vacío");
      }

      // Generar un nombre único para el archivo
      String originalFileName = file.getOriginalFilename();
      String fileExtension = "";
      if (originalFileName != null && originalFileName.contains(".")) {
        fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
      }
      String fileName = UUID.randomUUID().toString() + fileExtension;

      // Seleccionar el directorio adecuado según el tipo de archivo
      Path targetPath = isVideo ? videoStoragePath : fileStoragePath;

      // Guardar el archivo en el sistema de archivos
      Path targetLocation = targetPath.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      return fileName;
    } catch (IOException ex) {
      throw new RuntimeException(
          "No se pudo almacenar el archivo. Por favor, inténtelo de nuevo", ex);
    }
  }

  public Path getFilePath(String fileName) {
    return getFilePath(fileName, false);
  }

  public Path getFilePath(String fileName, boolean isVideo) {
    return isVideo ? videoStoragePath.resolve(fileName) : fileStoragePath.resolve(fileName);
  }
}
