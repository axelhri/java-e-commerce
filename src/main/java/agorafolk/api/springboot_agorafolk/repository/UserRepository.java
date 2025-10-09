package agorafolk.api.springboot_agorafolk.repository;

import agorafolk.api.springboot_agorafolk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);
}
