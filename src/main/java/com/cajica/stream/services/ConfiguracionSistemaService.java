package com.cajica.stream.services;

import com.cajica.stream.entities.ConfiguracionSistema;
import com.cajica.stream.repositories.ConfiguracionSistemaRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracionSistemaService {

  private static final String CLAVE_EMISOR_NOMBRE = "diploma.emisor.nombre";
  private static final String CLAVE_EMISOR_CARGO = "diploma.emisor.cargo";

  private final ConfiguracionSistemaRepository repository;

  @Autowired
  public ConfiguracionSistemaService(ConfiguracionSistemaRepository repository) {
    this.repository = repository;
  }

  public String getEmisorNombre() {
    return repository
        .findByClave(CLAVE_EMISOR_NOMBRE)
        .map(ConfiguracionSistema::getValor)
        .orElse("");
  }

  public String getEmisorCargo() {
    return repository
        .findByClave(CLAVE_EMISOR_CARGO)
        .map(ConfiguracionSistema::getValor)
        .orElse("");
  }

  public void guardarEmisor(String nombre, String cargo) {
    guardarOActualizar(CLAVE_EMISOR_NOMBRE, nombre, "Nombre completo de quien emite el diploma");
    guardarOActualizar(CLAVE_EMISOR_CARGO, cargo, "Cargo de quien emite el diploma");
  }

  private void guardarOActualizar(String clave, String valor, String descripcion) {
    ConfiguracionSistema config =
        repository
            .findByClave(clave)
            .orElseGet(
                () -> {
                  ConfiguracionSistema nueva = new ConfiguracionSistema();
                  nueva.setClave(clave);
                  nueva.setDescripcion(descripcion);
                  return nueva;
                });
    config.setValor(valor != null ? valor.trim() : "");
    config.setUpdatedAt(LocalDateTime.now());
    repository.save(config);
  }
}
