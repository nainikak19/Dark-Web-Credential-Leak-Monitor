package com.dwclm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dwclm.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	long countByBreachedTrue();

	boolean existsByEmail(String email);
}
