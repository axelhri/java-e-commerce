package agorafolk.api.springboot_agorafolk.interfaces;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationRequest;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import agorafolk.api.springboot_agorafolk.dto.RefreshTokenResponse;

public interface AuthenticationServiceInterface {
  AuthenticationResponse register(AuthenticationRequest registerRequest);

  AuthenticationResponse login(AuthenticationRequest loginRequest);

  RefreshTokenResponse refreshToken(String refreshToken);
}
