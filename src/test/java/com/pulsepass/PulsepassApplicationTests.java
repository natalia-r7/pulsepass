package com.pulsepass;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;

import com.pulsepass.entity.Artist;
import com.pulsepass.entity.Event;
import com.pulsepass.entity.EventCategory;
import com.pulsepass.entity.EventStatus;
import com.pulsepass.entity.Venue;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.VenueRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.pulsepass.entity.Ticket;
import com.pulsepass.entity.TicketStatus;
import com.pulsepass.entity.TicketType;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.entity.User;
import com.pulsepass.repository.UserRepository;



@Testcontainers
@SpringBootTest
class PulsepassApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void contextLoads() {
    }
	@Autowired
private ArtistRepository artistRepository;
@Autowired
private VenueRepository venueRepository;

@Autowired
private EventRepository eventRepository;
@Autowired
private TicketRepository ticketRepository;
@Autowired
private UserRepository userRepository;
@PersistenceContext
private EntityManager entityManager;
@Test
void shouldFindInitialArtists() {
    var artists = artistRepository.findAll();

    assertEquals(5, artists.size());
}
@Test
void shouldFindArtistByStageName() {
    var artist = artistRepository.findByStageName("Solar Beat");

    assertEquals("Solar Beat", artist.orElseThrow().getStageName());
}
@Test
void shouldFindEventByEventCode() {
    Venue venue = new Venue();
    venue.setCode("VEN-001");
    venue.setName("Arena Pulse");
    venue.setCity("Santa Marta");
    venue.setAddress("Carrera 1 #1-1");
    venue.setCapacity(1000);
    venue.setActive(true);

    venue = venueRepository.save(venue);

    Event event = new Event();
    event.setEventCode("EVT-001");
    event.setName("PulsePass Fest");
    event.setDescription("Evento de prueba");
    event.setCategory(EventCategory.MUSIC);
    event.setStatus(EventStatus.PUBLISHED);
    event.setEventDate(LocalDateTime.of(2026, 10, 15, 19, 0));
    event.setMinimumAge(18);
    event.setVenue(venue);

    event = eventRepository.save(event);

    var foundEvent = eventRepository.findByEventCode("EVT-001");

    assertEquals("PulsePass Fest", foundEvent.orElseThrow().getName());
}
@Test
void shouldFindPublishedEventsOrderedByDate() {
    var events = eventRepository.findByStatusOrderByEventDateAsc(
        EventStatus.PUBLISHED
    );

    assertEquals(1, events.size());
    assertEquals("EVT-001", events.get(0).getEventCode());
}
@Test
void shouldFindEventsByVenueCode() {
    var events = eventRepository.findByVenue_Code("VEN-001");

    assertEquals(1, events.size());
    assertEquals("EVT-001", events.get(0).getEventCode());
}
@Test
void shouldFindTicketsByUserEmailAndStatus() {
    User user = new User();
    user.setUsername("natalia");
    user.setEmail("natalia@example.com");
    user.setActive(true);

    user = userRepository.save(user);

    Ticket ticket = new Ticket();
    ticket.setTicketCode("TCK-001");
    ticket.setType(TicketType.GENERAL);
    ticket.setPrice(new BigDecimal("50000.00"));
    ticket.setStatus(TicketStatus.PAID);
    ticket.setPurchaseDate(LocalDateTime.now());
    ticket.setUser(user);

    Event event = eventRepository.findByEventCode("EVT-001")
    .orElseThrow();

ticket.setEvent(event);

ticketRepository.save(ticket);

var tickets = ticketRepository.findByUser_EmailAndStatus(
    "natalia@example.com",
    TicketStatus.PAID
);

assertEquals(1, tickets.size());
}
@Test
void shouldFindTicketsByUserEmailAndStatusUsingJpql() {
    User user = new User();
    user.setUsername("natalia.jpql");
    user.setEmail("natalia.jpql@example.com");
    user.setActive(true);

    user = userRepository.save(user);

    Event event = eventRepository.findByEventCode("EVT-001")
        .orElseThrow();

    Ticket ticket = new Ticket();
    ticket.setTicketCode("TCK-JPQL-001");
    ticket.setType(TicketType.GENERAL);
    ticket.setPrice(new BigDecimal("50000.00"));
    ticket.setStatus(TicketStatus.PAID);
    ticket.setPurchaseDate(LocalDateTime.now());
    ticket.setUser(user);
    ticket.setEvent(event);

    ticketRepository.save(ticket);

    var tickets = ticketRepository.findTicketsByUserEmailAndStatus(
        "natalia.jpql@example.com",
        TicketStatus.PAID
    );

    assertEquals(1, tickets.size());
}
@Transactional
@Test
void shouldFindEventsByArtist() {
    Event event = eventRepository.findByEventCode("EVT-001")
        .orElseThrow();

    Artist artist = artistRepository.findByStageName("Solar Beat")
        .orElseThrow();

    entityManager.createNativeQuery(
        "INSERT INTO event_artists (event_id, artist_id) VALUES (:eventId, :artistId)"
    )
    .setParameter("eventId", event.getId())
    .setParameter("artistId", artist.getId())
    .executeUpdate();

    var events = eventRepository.findEventsByArtist("Solar Beat");

    assertEquals(1, events.size());
}
}
