package neora.repository;

import java.util.Optional;
import java.util.UUID;
import neora.entity.Bookmark;
import neora.entity.Product;
import neora.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
  Optional<Bookmark> findByUserAndProduct(User user, Product product);
}
