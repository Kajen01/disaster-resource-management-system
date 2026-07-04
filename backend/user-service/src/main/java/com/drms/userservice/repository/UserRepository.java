package com.drms.userservice.repository;

import com.drms.userservice.entity.Role;
import com.drms.userservice.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByIdAndRole(Long id, Role role);
}
