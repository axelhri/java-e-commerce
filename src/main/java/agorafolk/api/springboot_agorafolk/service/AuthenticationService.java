package agorafolk.api.springboot_agorafolk.service;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import agorafolk.api.springboot_agorafolk.dto.AuthenticationRequest;
import agorafolk.api.springboot_agorafolk.entity.User;
import agorafolk.api.springboot_agorafolk.interfaces.AuthenticationServiceInterface;
import agorafolk.api.springboot_agorafolk.mapper.UserMapper;
import agorafolk.api.springboot_agorafolk.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationService implements AuthenticationServiceInterface {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  public AuthenticationResponse register(AuthenticationRequest registerRequest) {

    if (userRepository.existsByEmail(registerRequest.email())) {
      throw new IllegalArgumentException("Invalid informations"); // REFACTOR : custom exception to be added
    }

    User user = userMapper.toUserEntity(registerRequest);
    userRepository.save(user);

    String jwt = jwtService.generateToken(user);

    return new AuthenticationResponse(jwt, user.getId());
  }

  @Override
  public AuthenticationResponse login(AuthenticationRequest loginRequest) {
    User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    String jwt = jwtService.generateToken(user);

    return new AuthenticationResponse(jwt, user.getId());
  }
}
