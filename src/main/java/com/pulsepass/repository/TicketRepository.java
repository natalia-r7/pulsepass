package com.pulsepass.repository;

import com.pulsepass.entity.Ticket;
import com.pulsepass.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByUser_Email(String email);

    List<Ticket> findByUser_EmailAndStatus(String email, TicketStatus status);

    @Query("""
        SELECT t
        FROM Ticket t
        WHERE t.user.email = :email
        AND t.status = :status
        """)
    List<Ticket> findTicketsByUserEmailAndStatus(
        @Param("email") String email,
        @Param("status") TicketStatus status
    );

    List<Ticket> findByEvent_EventCodeAndStatus(
        String eventCode,
        TicketStatus status
    );

    List<Ticket> findByEvent_EventDateAfterOrderByEvent_EventDateAsc(
        LocalDateTime date
    );
}