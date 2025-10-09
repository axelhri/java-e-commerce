package agorafolk.api.springboot_agorafolk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterRequest {
  private final String email;
  private final String password;
}
