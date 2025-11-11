package ecom;

import ecom.config.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootEcommerceApplicationTests extends PostgresTestContainer {

  @Test
  void contextLoads() {
    // This test is intentionally empty.
    // It only checks that the Spring application context loads successfully.
  }
}
