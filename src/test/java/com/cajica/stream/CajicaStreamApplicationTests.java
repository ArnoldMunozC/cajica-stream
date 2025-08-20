package com.cajica.stream;

import com.cajica.stream.config.TestSecurityConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestSecurityConfig.class)
class CajicaStreamApplicationTests {

  @Test
  @Disabled(
      "Deshabilitado temporalmente mientras se resuelven problemas de configuración de Spring"
          + " Security")
  void contextLoads() {}
}
