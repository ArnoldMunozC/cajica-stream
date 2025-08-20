package com.cajica.stream.services;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio para limitar la frecuencia de solicitudes de recuperación de contraseña y prevenir
 * ataques de fuerza bruta.
 */
@Service
public class SecurityRateLimiterService {

  private static final Logger logger = LoggerFactory.getLogger(SecurityRateLimiterService.class);

  // Mapa para almacenar los intentos por dirección de correo electrónico
  private final Map<String, AttemptInfo> attemptMap = new ConcurrentHashMap<>();

  // Configuración de límites
  private static final int MAX_ATTEMPTS = 3; // Máximo número de intentos en el período
  private static final int LOCKOUT_MINUTES = 30; // Tiempo de bloqueo en minutos
  private static final int ATTEMPT_WINDOW_MINUTES = 60; // Ventana de tiempo para contar intentos

  /**
   * Verifica si se permite una nueva solicitud para el correo electrónico dado
   *
   * @param email Dirección de correo electrónico
   * @return true si se permite la solicitud, false si está bloqueada
   */
  public boolean allowRequest(String email) {
    String normalizedEmail = email.toLowerCase().trim();
    LocalDateTime now = LocalDateTime.now();

    // Limpiar entradas antiguas
    cleanupOldEntries();

    // Obtener o crear información de intentos para este email
    AttemptInfo info =
        attemptMap.computeIfAbsent(normalizedEmail, k -> new AttemptInfo(0, null, now));

    // Verificar si está bloqueado
    if (info.lockedUntil != null && now.isBefore(info.lockedUntil)) {
      logger.warn(
          "Solicitud bloqueada para email: {}. Bloqueado hasta: {}",
          normalizedEmail,
          info.lockedUntil);
      return false;
    }

    // Reiniciar contador si la ventana de intentos ha expirado
    if (info.firstAttempt.plusMinutes(ATTEMPT_WINDOW_MINUTES).isBefore(now)) {
      info.attempts = 0;
      info.firstAttempt = now;
    }

    // Incrementar contador de intentos
    info.attempts++;

    // Bloquear si excede el límite
    if (info.attempts > MAX_ATTEMPTS) {
      info.lockedUntil = now.plusMinutes(LOCKOUT_MINUTES);
      logger.warn(
          "Email bloqueado por exceder límite de intentos: {}. Bloqueado hasta: {}",
          normalizedEmail,
          info.lockedUntil);
      return false;
    }

    return true;
  }

  /**
   * Registra un intento exitoso y reinicia el contador
   *
   * @param email Dirección de correo electrónico
   */
  public void registerSuccessfulAttempt(String email) {
    String normalizedEmail = email.toLowerCase().trim();
    attemptMap.remove(normalizedEmail);
  }

  /** Limpia entradas antiguas del mapa para evitar crecimiento indefinido */
  private void cleanupOldEntries() {
    LocalDateTime cutoff = LocalDateTime.now().minusHours(2);
    attemptMap
        .entrySet()
        .removeIf(
            entry -> {
              AttemptInfo info = entry.getValue();
              return (info.lockedUntil == null || info.lockedUntil.isBefore(cutoff))
                  && info.firstAttempt.isBefore(cutoff);
            });
  }

  /** Clase interna para almacenar información de intentos */
  private static class AttemptInfo {
    int attempts;
    LocalDateTime lockedUntil;
    LocalDateTime firstAttempt;

    public AttemptInfo(int attempts, LocalDateTime lockedUntil, LocalDateTime firstAttempt) {
      this.attempts = attempts;
      this.lockedUntil = lockedUntil;
      this.firstAttempt = firstAttempt;
    }
  }
}
