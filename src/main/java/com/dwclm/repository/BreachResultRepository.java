package com.dwclm.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dwclm.model.BreachResult;

public interface BreachResultRepository extends JpaRepository<BreachResult, Long> {

	List<BreachResult> findByUserId(Long userId);

	List<BreachResult> findByBreachDateAfter(LocalDate date);

	List<BreachResult> findByBreachDate(LocalDate date);

	@Query("SELECT br.user.email, COUNT(br) FROM BreachResult br GROUP BY br.user.email ORDER BY COUNT(br) DESC")
	List<Object[]> findTopBreachedEmails();


	List<BreachResult> findByUserEmail(String email);

	@Query("SELECT COUNT(DISTINCT br.user.id) FROM BreachResult br")
	long countDistinctUserId();

	boolean existsByUserIdAndBreachName(Long userId, String breachName);
}
