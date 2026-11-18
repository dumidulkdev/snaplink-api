package com.snaplink.repository;

import com.snaplink.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {

    Optional<Link> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT l FROM Link l WHERE l.active = true AND l.expiresAt IS NOT NULL AND l.expiresAt < :now")
    List<Link> findExpiredActiveLinks(@Param("now") Instant now);

    @Modifying
    @Query("UPDATE Link l SET l.active = false WHERE l.id IN :ids")
    int deactivateByIds(@Param("ids") List<Long> ids);

    @Modifying
    void deleteByIdIn(List<Long> ids);
}