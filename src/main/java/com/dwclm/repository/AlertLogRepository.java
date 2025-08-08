package com.dwclm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dwclm.model.AlertLog;

public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {
}
