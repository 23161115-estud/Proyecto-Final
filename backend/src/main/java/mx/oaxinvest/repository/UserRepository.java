package mx.oaxinvest.repository;

import mx.oaxinvest.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "role")
    Optional<User> findWithRoleById(Integer id);

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "role")
    Optional<User> findByVerificationToken(String token);

    @Override
    @EntityGraph(attributePaths = "role")
    Page<User> findAll(Specification<User> spec, Pageable pageable);
}
