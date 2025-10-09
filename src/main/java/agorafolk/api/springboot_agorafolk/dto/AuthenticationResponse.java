package agorafolk.api.springboot_agorafolk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AuthenticationResponse {
  private final String token;
  private final UUID id;
}
