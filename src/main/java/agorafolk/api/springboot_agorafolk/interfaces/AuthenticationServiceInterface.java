package agorafolk.api.springboot_agorafolk.interfaces;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import agorafolk.api.springboot_agorafolk.dto.RegisterRequest;

public interface AuthenticationServiceInterface {
  AuthenticationResponse register(RegisterRequest registerRequest);
}
