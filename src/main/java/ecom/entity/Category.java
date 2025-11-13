package ecom.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categories")
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID id;

  @Column(length = 50, nullable = false, unique = true)
  private String name;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp @Column private Instant updatedAt;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "category_parent",
      joinColumns = @JoinColumn(name = "child_id"),
      inverseJoinColumns = @JoinColumn(name = "parent_id"))
  private Set<Category> parentCategory = new HashSet<>();

  @ManyToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY)
  private Set<Category> childrenCategory = new HashSet<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<Product> products = new HashSet<>();
}
