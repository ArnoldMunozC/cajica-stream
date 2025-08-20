package com.cajica.stream.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
public class MultipartConfig {

  @Value("${spring.servlet.multipart.max-file-size:3072MB}")
  private String maxFileSize;

  @Value("${spring.servlet.multipart.max-request-size:3072MB}")
  private String maxRequestSize;

  @Bean
  public MultipartResolver multipartResolver() {
    return new StandardServletMultipartResolver();
  }

  @Bean
  public MultipartConfigElement multipartConfigElement() {
    MultipartConfigFactory factory = new MultipartConfigFactory();

    // Configurar límites de tamaño con valores explícitos (3GB)
    factory.setMaxFileSize(DataSize.parse("3072MB"));
    factory.setMaxRequestSize(DataSize.parse("3072MB"));

    // Configurar directorio temporal para archivos grandes
    factory.setLocation(System.getProperty("java.io.tmpdir"));

    return factory.createMultipartConfig();
  }
}
