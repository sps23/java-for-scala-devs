package io.github.sps23.testing.unittesting;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for user data access. This abstraction makes services
 * testable by allowing easy mocking of database operations.
 */
public interface UserRepository {
    Optional<User> findById(String id);

    List<User> findAll();

    User save(User user);

    void deleteById(String id);

    boolean existsById(String id);
}
