package agorafolk.api.springboot_agorafolk.mapper;

import agorafolk.api.springboot_agorafolk.dto.RegisterRequest;
import agorafolk.api.springboot_agorafolk.entity.User;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public User toUserEntity(@NonNull RegisterRequest dto) {
     return User.builder()
            .email(dto.getEmail())
            .password(dto.getPassword())
            .build();
  }
}
