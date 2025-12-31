package ecom.repository;

import ecom.entity.MailConfirmation;
import ecom.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailConfirmationRepository extends JpaRepository<MailConfirmation, UUID> {
  Optional<MailConfirmation> findByToken(String token);

  void deleteByUser(User user);
}
