package neora.repository;

import neora.entity.MailConfirmation;
import neora.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailConfirmationRepository extends JpaRepository<MailConfirmation, UUID> {
  Optional<MailConfirmation> findByToken(String token);

  void deleteByUser(User user);
}
