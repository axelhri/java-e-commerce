package agorafolk.api.springboot_agorafolk.interfaces;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationRequest;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AuthenticationServiceInterface {
  AuthenticationResponse register(AuthenticationRequest registerRequest);

  AuthenticationResponse login(AuthenticationRequest loginRequest);

  void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
