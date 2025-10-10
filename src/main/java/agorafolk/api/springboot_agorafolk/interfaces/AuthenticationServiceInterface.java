package agorafolk.api.springboot_agorafolk.interfaces;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationRequest;

public interface AuthenticationServiceInterface {
  AuthenticationResponse register(AuthenticationRequest registerRequest);
  AuthenticationResponse login(AuthenticationRequest loginRequest);
}
