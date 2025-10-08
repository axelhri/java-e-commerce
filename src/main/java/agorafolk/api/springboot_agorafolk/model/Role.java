package agorafolk.api.springboot_agorafolk.model;

import static agorafolk.api.springboot_agorafolk.model.Permission.*;

import java.util.Collections;
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

  private final Set<SimpleGrantedAuthority> authoritiesCache;

  Role(Set<Permission> permissions) {
    this.permissions = permissions;
    this.authoritiesCache =
        Collections.unmodifiableSet(
            Stream.concat(
                    permissions.stream().map(Permission::toAuthority),
                    Stream.of(new SimpleGrantedAuthority("ROLE_" + name())))
                .collect(Collectors.toSet()));
  }

  public Set<SimpleGrantedAuthority> getAuthorities() {
    return authoritiesCache;
  }
}
