package com.example.Payroll.Repository;

import com.example.Payroll.Entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // ✅ Date-only filter
    Page<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // ✅ Search-only filter
    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:search IS NULL OR :search = '' 
            OR LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.user.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY a.createdAt DESC
        """)
    Page<AuditLog> findBySearch(@Param("search") String search, Pageable pageable);

    // ✅ Combined date range + search filter
    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.createdAt BETWEEN :start AND :end
          AND (:search IS NULL OR :search = '' 
            OR LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.user.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY a.createdAt DESC
        """)
    Page<AuditLog> findByCreatedAtBetweenAndSearch(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end,
                                                   @Param("search") String search,
                                                   Pageable pageable);
}
