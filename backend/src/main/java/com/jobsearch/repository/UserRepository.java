package com.jobsearch.repository;

import com.jobsearch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring automatically writes this SQL:
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Check if email already exists before registering
    boolean existsByEmail(String email);
}
