package com.cajica.stream.services;

import com.cajica.stream.entities.Rol;
import com.cajica.stream.entities.Usuario;
import com.cajica.stream.repositories.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

  private static final Logger logger = LoggerFactory.getLogger(UsuarioDetailsService.class);

  private final UsuarioRepository usuarioRepository;

  @Autowired
  public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    logger.debug("Cargando detalles de usuario para autenticación. username={}", username);
    Usuario usuario =
        usuarioRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    // Cargar roles desde la base de datos
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

    // Añadir roles como autoridades
    for (Rol rol : usuario.getRoles()) {
      authorities.add(new SimpleGrantedAuthority(rol.getNombre()));
    }

    // Si no tiene roles, asignar un rol por defecto (esto no debería ocurrir normalmente)
    if (authorities.isEmpty()) {
      authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    }

    logger.info(
        "Usuario cargado para autenticación. username={}, rolesCount={}",
        usuario.getUsername(),
        authorities.size());

    return new User(
        usuario.getUsername(),
        usuario.getPassword(),
        usuario.isActivo(),
        true, // account non-expired
        true, // credentials non-expired
        true, // account non-locked
        authorities);
  }
}
