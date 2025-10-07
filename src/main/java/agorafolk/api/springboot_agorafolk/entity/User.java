package agorafolk.api.springboot_agorafolk.entity;

import agorafolk.api.springboot_agorafolk.model.Role;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "users",
    uniqueConstraints = {@UniqueConstraint(columnNames = "email")})
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID id;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 255)
  private String password;

  @Builder.Default
  @Column(nullable = false)
  private Integer points = 10000;

  @ElementCollection(fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role")
  private Set<Role> roles = EnumSet.of(Role.USER);

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = true)
  private Instant updatedAt;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false)
  private Cart cart;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();
    for (Role role : roles) {
      authorities.addAll(role.getAuthorities());
    }
    return authorities;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
