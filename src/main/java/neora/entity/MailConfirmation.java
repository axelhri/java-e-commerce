package neora.entity;

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
@Table(name = "mail_confirmations")
public class MailConfirmation {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID id;

  @Column(nullable = false, unique = true)
  private String token;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant expiresAt;

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }
}
