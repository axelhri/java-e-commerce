package agorafolk.api.springboot_agorafolk.service;

import agorafolk.api.springboot_agorafolk.dto.AuthenticationResponse;
import agorafolk.api.springboot_agorafolk.dto.RegisterRequest;
import agorafolk.api.springboot_agorafolk.entity.User;
import agorafolk.api.springboot_agorafolk.mapper.UserMapper;
import agorafolk.api.springboot_agorafolk.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final UserMapper userMapper;

  public AuthenticationResponse register(RegisterRequest registerRequest) {

    if (userRepository.existsByEmail(registerRequest.getEmail())) {
      throw new IllegalArgumentException("Invalid informations"); // REFACTOR : custom exception to be added
    }

    User user = userMapper.toUserEntity(registerRequest);
    userRepository.save(user);

    String jwt = jwtService.generateToken(user);

    return AuthenticationResponse
            .builder()
            .token(jwt)
            .id(user.getId())
            .build();
  }
}
