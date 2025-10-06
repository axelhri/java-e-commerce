package agorafolk.api.springboot_agorafolk.model;

import static agorafolk.api.springboot_agorafolk.model.Permission.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RequiredArgsConstructor
public enum Role {
  USER(Set.of(USER_READ, USER_CREATE, USER_DELETE, USER_UPDATE)),
  ADMIN(
      Set.of(
          USER_READ,
          USER_CREATE,
          USER_DELETE,
          USER_UPDATE,
          ADMIN_CREATE,
          ADMIN_DELETE,
          ADMIN_READ,
          ADMIN_UPDATE));

  @Getter private final Set<Permission> permissions;

  public Set<SimpleGrantedAuthority> getAuthorities() {
    return Stream.concat(
            getPermissions().stream().map(Permission::toAuthority),
            Stream.of(new SimpleGrantedAuthority("ROLE_" + this.name())))
        .collect(Collectors.toSet());
  }
}
