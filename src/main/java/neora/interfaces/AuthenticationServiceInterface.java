package neora.interfaces;

import neora.dto.AuthenticationRequest;
import neora.dto.AuthenticationResponse;
import neora.dto.RefreshTokenResponse;
import neora.dto.RegisterResponse;

public interface AuthenticationServiceInterface {
  RegisterResponse register(AuthenticationRequest registerRequest);

  AuthenticationResponse login(AuthenticationRequest loginRequest);

  RefreshTokenResponse refreshToken(String refreshToken);
}
