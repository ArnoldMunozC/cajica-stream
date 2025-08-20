package com.cajica.stream.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    // Obtener el código de estado del error
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

    // Obtener el mensaje de error
    Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

    // Obtener la excepción
    Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

    // Agregar información al modelo
    model.addAttribute("status", status != null ? status : "Desconocido");
    model.addAttribute("error", message != null ? message : "Ha ocurrido un error");
    model.addAttribute(
        "message", throwable != null ? throwable.getMessage() : "No hay detalles adicionales");

    // Personalizar la página de error según el código de estado
    if (status != null) {
      int statusCode = Integer.parseInt(status.toString());

      if (statusCode == HttpStatus.NOT_FOUND.value()) {
        return "error/404";
      } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
        return "error/403";
      } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
        return "error/500";
      }
    }

    // Página de error genérica para otros casos
    return "error/general";
  }
}
