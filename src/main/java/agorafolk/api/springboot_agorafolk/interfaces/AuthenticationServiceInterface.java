package agorafolk.api.springboot_agorafolk.interfaces;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationRequest;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;

public interface AuthenticationServiceInterface {
  AuthenticationResponse register(AuthenticationRequest registerRequest);

  AuthenticationResponse login(AuthenticationRequest loginRequest);

  AuthenticationResponse refreshToken(String refreshToken);
}
