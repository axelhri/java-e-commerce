package agorafolk.api.springboot_agorafolk.entity;

import agorafolk.api.springboot_agorafolk.model.TokenType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tokens")
public class Token {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID id;

  @Column(nullable = false, unique = true)
  private String token;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TokenType tokenType;

  @Builder.Default
  @Column(nullable = false)
  private boolean expired = false;

  @Builder.Default
  @Column(nullable = false)
  private boolean revoked = false;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}
