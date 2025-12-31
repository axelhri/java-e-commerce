package ecom.interfaces;

import ecom.dto.AuthenticationRequest;
import ecom.dto.AuthenticationResponse;
import ecom.dto.RefreshTokenResponse;
import ecom.dto.RegisterResponse;

public interface AuthenticationServiceInterface {
  RegisterResponse register(AuthenticationRequest registerRequest);

  AuthenticationResponse login(AuthenticationRequest loginRequest);

  RefreshTokenResponse refreshToken(String refreshToken);
}
