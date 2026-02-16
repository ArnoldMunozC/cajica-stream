package com.cajica.stream.services;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

  private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

  @Value("${app.upload.dir:${user.home}/cajica-stream/uploads/images}")
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
      logger.info("Directorio de almacenamiento de imágenes inicializado en: {}", fileStoragePath);

      // Inicializar directorio para videos
      videoStoragePath = Paths.get(videoUploadDir).toAbsolutePath().normalize();
      Files.createDirectories(videoStoragePath);
      logger.info("Directorio de almacenamiento de videos inicializado en: {}", videoStoragePath);
    } catch (IOException e) {
      logger.error("No se pudo inicializar directorios de almacenamiento", e);
      throw new RuntimeException("No se pudo crear el directorio de almacenamiento de archivos", e);
    }
  }

  public String storeFile(MultipartFile file) {
    return storeFile(file, false);
  }

  public String storeImageDeduplicated(MultipartFile file) {
    try {
      if (file == null || file.isEmpty()) {
        logger.warn("Intento de almacenar imagen vacía");
        throw new RuntimeException("No se puede almacenar un archivo vacío");
      }

      String originalFileName = file.getOriginalFilename();
      String fileExtension = "";
      if (originalFileName != null && originalFileName.contains(".")) {
        fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
      }

      String sha256 = sha256Hex(file.getInputStream());
      String fileName = sha256 + fileExtension;
      Path targetLocation = fileStoragePath.resolve(fileName);

      if (!Files.exists(targetLocation)) {
        Files.copy(file.getInputStream(), targetLocation);
        logger.info(
            "Imagen almacenada (deduplicada) exitosamente. fileName={}, sizeBytes={}",
            fileName,
            file.getSize());
      } else {
        logger.info("Imagen ya existía, se reutiliza. fileName={}", fileName);
      }

      return fileName;
    } catch (IOException ex) {
      logger.error(
          "Error almacenando imagen deduplicada. originalFileName={}",
          file == null ? null : file.getOriginalFilename(),
          ex);
      throw new RuntimeException(
          "No se pudo almacenar la imagen. Por favor, inténtelo de nuevo", ex);
    }
  }

  public String storeFile(MultipartFile file, boolean isVideo) {
    try {
      // Verificar que el archivo no esté vacío
      if (file.isEmpty()) {
        logger.warn("Intento de almacenar archivo vacío. isVideo={}", isVideo);
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

      logger.info(
          "Archivo almacenado exitosamente. isVideo={}, fileName={}, sizeBytes={}",
          isVideo,
          fileName,
          file.getSize());

      return fileName;
    } catch (IOException ex) {
      logger.error(
          "Error almacenando archivo. isVideo={}, originalFileName={}",
          isVideo,
          file.getOriginalFilename(),
          ex);
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

  public boolean deleteFile(String fileName) {
    return deleteFile(fileName, false);
  }

  public boolean deleteFile(String fileName, boolean isVideo) {
    try {
      Path filePath = getFilePath(fileName, isVideo);
      boolean deleted = Files.deleteIfExists(filePath);
      logger.info(
          "Eliminación de archivo ejecutada. isVideo={}, fileName={}, deleted={}",
          isVideo,
          fileName,
          deleted);
      return deleted;
    } catch (IOException e) {
      logger.error("Error eliminando archivo. isVideo={}, fileName={}", isVideo, fileName, e);
      return false;
    }
  }

  private static String sha256Hex(InputStream is) throws IOException {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] buffer = new byte[8192];
      int read;
      while ((read = is.read(buffer)) != -1) {
        digest.update(buffer, 0, read);
      }
      return HexFormat.of().formatHex(digest.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 no disponible", e);
    } finally {
      try {
        is.close();
      } catch (IOException ignored) {
      }
    }
  }
}
