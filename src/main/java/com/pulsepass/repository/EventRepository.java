package com.pulsepass.repository;

import com.pulsepass.entity.Event;
import com.pulsepass.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventCode(String eventCode);

    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    List<Event> findByVenue_Code(String venueCode);

    @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN e.artists a
        WHERE a.stageName = :stageName
        """)
    List<Event> findEventsByArtist(@Param("stageName") String stageName);

    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.event.id = :eventId
        AND t.status = com.pulsepass.entity.TicketStatus.PAID
        """)
    long countPaidTicketsByEvent(@Param("eventId") Long eventId);

    @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN e.artists a
        WHERE e.venue.city = :city
        AND a.stageName = :stageName
        """)
    List<Event> findEventsByCityAndArtist(
        @Param("city") String city,
        @Param("stageName") String stageName
    );

    @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN e.artists a
        WHERE e.status = com.pulsepass.entity.EventStatus.PUBLISHED
        AND e.eventDate > :date
        AND e.venue.city = :city
        AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistText, '%'))
        ORDER BY e.eventDate ASC
        """)
    List<Event> findRecommendedEvents(
        @Param("date") LocalDateTime date,
        @Param("city") String city,
        @Param("artistText") String artistText
    );
}