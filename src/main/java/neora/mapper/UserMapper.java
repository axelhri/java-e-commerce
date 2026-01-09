package neora.mapper;

import neora.dto.AuthenticationRequest;
import neora.entity.User;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public User toUserEntity(@NonNull AuthenticationRequest dto) {
    return User.builder().email(dto.email()).password(dto.password()).build();
  }
}
