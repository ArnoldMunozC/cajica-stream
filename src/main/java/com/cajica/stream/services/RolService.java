package com.cajica.stream.services;

import com.cajica.stream.entities.Rol;
import com.cajica.stream.repositories.RolRepository;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolService {

  private final RolRepository rolRepository;

  @Autowired
  public RolService(RolRepository rolRepository) {
    this.rolRepository = rolRepository;
  }

  @PostConstruct
  public void inicializarRoles() {
    // Crear roles por defecto si no existen
    if (rolRepository.findByNombre("ROLE_ADMIN").isEmpty()) {
      rolRepository.save(new Rol("ROLE_ADMIN"));
    }
    if (rolRepository.findByNombre("ROLE_USER").isEmpty()) {
      rolRepository.save(new Rol("ROLE_USER"));
    }
  }

  @Transactional(readOnly = true)
  public Optional<Rol> findByNombre(String nombre) {
    return rolRepository.findByNombre(nombre);
  }

  @Transactional(readOnly = true)
  public boolean esElPrimerUsuario() {
    return rolRepository.count() == 0;
  }
}
