package agorafolk.api.springboot_agorafolk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringbootAgorafolkApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringbootAgorafolkApplication.class, args);
  }
}
