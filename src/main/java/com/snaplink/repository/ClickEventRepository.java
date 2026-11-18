package com.snaplink.repository;

import com.snaplink.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByLinkId(Long linkId);

    @Query(value = """
            SELECT CAST(c.clicked_at AS DATE) AS click_day, COUNT(*) AS cnt
            FROM click_events c
            WHERE c.link_id = :linkId AND c.clicked_at >= :since
            GROUP BY CAST(c.clicked_at AS DATE)
            ORDER BY click_day
            """, nativeQuery = true)
    List<Object[]> countClicksPerDay(@Param("linkId") Long linkId, @Param("since") Instant since);

    @Query("""
            SELECT COALESCE(e.referrer, ''), COUNT(e)
            FROM ClickEvent e
            WHERE e.link.id = :linkId
            GROUP BY COALESCE(e.referrer, '')
            ORDER BY COUNT(e) DESC
            """)
    List<Object[]> topReferrers(@Param("linkId") Long linkId);

    @Query("""
            SELECT COALESCE(e.userAgent, ''), COUNT(e)
            FROM ClickEvent e
            WHERE e.link.id = :linkId
            GROUP BY COALESCE(e.userAgent, '')
            ORDER BY COUNT(e) DESC
            """)
    List<Object[]> topUserAgents(@Param("linkId") Long linkId);
}