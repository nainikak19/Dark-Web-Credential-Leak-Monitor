package com.dwclm.repository;

import com.dwclm.model.BreachedEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BreachedEmailRepository extends JpaRepository<BreachedEmail, Long> {
    Optional<BreachedEmail> findByEmailIgnoreCase(String email);
}
