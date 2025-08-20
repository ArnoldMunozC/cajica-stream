package com.cajica.stream.config;

import com.cajica.stream.entities.Rol;
import com.cajica.stream.repositories.RolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
  private final RolRepository rolRepository;

  @Autowired
  public DataInitializer(RolRepository rolRepository) {
    this.rolRepository = rolRepository;
  }

  @Override
  public void run(String... args) {
    logger.info("Inicializando datos de la aplicación...");
    inicializarRoles();
  }

  private void inicializarRoles() {
    logger.info("Verificando roles predeterminados...");

    // Crear rol ADMIN si no existe
    if (rolRepository.findByNombre("ROLE_ADMIN").isEmpty()) {
      logger.info("Creando rol ROLE_ADMIN");
      rolRepository.save(new Rol("ROLE_ADMIN"));
    }

    // Crear rol USER si no existe
    if (rolRepository.findByNombre("ROLE_USER").isEmpty()) {
      logger.info("Creando rol ROLE_USER");
      rolRepository.save(new Rol("ROLE_USER"));
    }

    logger.info("Inicialización de roles completada");
  }
}
