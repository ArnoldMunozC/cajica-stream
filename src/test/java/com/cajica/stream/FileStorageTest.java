package com.cajica.stream;

import com.cajica.stream.services.FileStorageService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
public class FileStorageTest {

  @Autowired private FileStorageService fileStorageService;

  @Test
  public void testVideoStorage() throws IOException {
    // Ruta al archivo de prueba
    String testVideoPath = "./test_video.mp4";
    File testVideoFile = new File(testVideoPath);

    System.out.println("=== INICIANDO PRUEBA DE ALMACENAMIENTO DE VIDEO ===");

    // Verificar que el archivo de prueba existe
    if (!testVideoFile.exists()) {
      System.out.println("El archivo de prueba no existe: " + testVideoPath);
      return;
    }
    System.out.println("Archivo de prueba encontrado: " + testVideoFile.getAbsolutePath());
    System.out.println("Tamaño del archivo: " + testVideoFile.length() + " bytes");

    // Crear un MultipartFile a partir del archivo de prueba
    FileInputStream input = new FileInputStream(testVideoFile);
    MultipartFile multipartFile =
        new MockMultipartFile("test_video.mp4", "test_video.mp4", "video/mp4", input);

    // Guardar el archivo usando FileStorageService
    String savedFileName = fileStorageService.storeFile(multipartFile, true);
    System.out.println("Archivo guardado con nombre: " + savedFileName);

    // Verificar que el archivo se guardó en la ubicación externa
    Path externalPath = Paths.get("/Users/arnmunoz/Documents/Learning/videos", savedFileName);
    boolean exists = Files.exists(externalPath);
    System.out.println("¿El archivo existe en la ubicación externa? " + exists);
    System.out.println("Ruta completa: " + externalPath.toString());

    if (exists) {
      System.out.println("Tamaño del archivo guardado: " + Files.size(externalPath) + " bytes");
      System.out.println(
          "¡PRUEBA EXITOSA! El video se guardó correctamente en la ubicación externa.");
    } else {
      System.out.println("ERROR: El archivo no se guardó en la ubicación externa.");

      // Verificar si se guardó en la ubicación antigua
      Path oldPath = Paths.get("./uploads/videos", savedFileName);
      boolean existsInOldPath = Files.exists(oldPath);
      System.out.println("¿El archivo existe en la ubicación antigua? " + existsInOldPath);
    }

    System.out.println("=== FIN DE LA PRUEBA ===");

    // Cerrar el stream
    input.close();
  }
}
