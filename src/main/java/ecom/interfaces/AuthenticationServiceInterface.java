package ecom.interfaces;

import ecom.dto.AuthenticationRequest;
import ecom.dto.AuthenticationResponse;
import ecom.dto.RefreshTokenResponse;

public interface AuthenticationServiceInterface {
  AuthenticationResponse register(AuthenticationRequest registerRequest);

  AuthenticationResponse login(AuthenticationRequest loginRequest);

  RefreshTokenResponse refreshToken(String refreshToken);
}
