package auth.service.repository;

import auth.service.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    @Query("select u from User u where u.email = :email")
    Optional<User> findWithRolesByEmail(@Param("email") String email);
}
