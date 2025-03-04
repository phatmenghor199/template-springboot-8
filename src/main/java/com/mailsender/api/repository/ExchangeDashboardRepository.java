package com.mailsender.api.repository;

import com.mailsender.api.models.ExchangeDashboard;
import com.mailsender.api.models.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ExchangeDashboardRepository extends JpaRepository<ExchangeDashboard, Long> {

    @Query("SELECT e FROM ExchangeDashboard e WHERE e.fetchedAt BETWEEN :startDate AND :endDate ORDER BY e.fetchedAt ASC")
    List<ExchangeDashboard> findExchangeDashboardsByFetchedAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
