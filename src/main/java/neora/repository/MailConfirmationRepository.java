package neora.repository;

import java.util.Optional;
import java.util.UUID;
import neora.entity.MailConfirmation;
import neora.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailConfirmationRepository extends JpaRepository<MailConfirmation, UUID> {
  Optional<MailConfirmation> findByToken(String token);

  void deleteByUser(User user);
}
