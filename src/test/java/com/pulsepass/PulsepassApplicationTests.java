package com.pulsepass;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.pulsepass.entity.Artist;
import com.pulsepass.entity.Event;
import com.pulsepass.entity.EventCategory;
import com.pulsepass.entity.EventStatus;
import com.pulsepass.entity.Ticket;
import com.pulsepass.entity.TicketStatus;
import com.pulsepass.entity.TicketType;
import com.pulsepass.entity.User;
import com.pulsepass.entity.Venue;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.VenueRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Testcontainers
@SpringBootTest
@Transactional
class PulsepassApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

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
    void contextLoads() {
    }

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

        eventRepository.save(event);

        var foundEvent = eventRepository.findByEventCode("EVT-001");

        assertEquals("PulsePass Fest", foundEvent.orElseThrow().getName());
    }

    @Test
    void shouldFindPublishedEventsOrderedByDate() {
        Venue venue = new Venue();
        venue.setCode("VEN-PUBLISHED");
        venue.setName("Arena Published");
        venue.setCity("Santa Marta");
        venue.setAddress("Carrera 2 #2-2");
        venue.setCapacity(2000);
        venue.setActive(true);

        venue = venueRepository.save(venue);

        Event event = new Event();
        event.setEventCode("EVT-PUBLISHED");
        event.setName("Published Event");
        event.setDescription("Evento publicado");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.of(2026, 10, 20, 19, 0));
        event.setMinimumAge(18);
        event.setVenue(venue);

        eventRepository.save(event);

        var events = eventRepository.findByStatusOrderByEventDateAsc(
                EventStatus.PUBLISHED
        );

        assertEquals(1, events.size());
        assertEquals("EVT-PUBLISHED", events.get(0).getEventCode());
    }

    @Test
    void shouldFindEventsByVenueCode() {
        Venue venue = new Venue();
        venue.setCode("VEN-002");
        venue.setName("Arena Venue");
        venue.setCity("Santa Marta");
        venue.setAddress("Carrera 3 #3-3");
        venue.setCapacity(1500);
        venue.setActive(true);

        venue = venueRepository.save(venue);

        Event event = new Event();
        event.setEventCode("EVT-VENUE");
        event.setName("Venue Event");
        event.setDescription("Evento por venue");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.of(2026, 11, 15, 19, 0));
        event.setMinimumAge(18);
        event.setVenue(venue);

        eventRepository.save(event);

        var events = eventRepository.findByVenue_Code("VEN-002");

        assertEquals(1, events.size());
        assertEquals("EVT-VENUE", events.get(0).getEventCode());
    }

    @Test
    void shouldFindTicketsByUserEmailAndStatus() {
        Venue venue = new Venue();
        venue.setCode("VEN-TICKET-001");
        venue.setName("Ticket Arena");
        venue.setCity("Santa Marta");
        venue.setAddress("Carrera 4 #4-4");
        venue.setCapacity(1000);
        venue.setActive(true);

        venue = venueRepository.save(venue);

        Event event = new Event();
        event.setEventCode("EVT-TICKET-001");
        event.setName("Ticket Event");
        event.setDescription("Evento de tickets");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.of(2026, 10, 15, 19, 0));
        event.setMinimumAge(18);
        event.setVenue(venue);

        event = eventRepository.save(event);

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
        Venue venue = new Venue();
        venue.setCode("VEN-JPQL");
        venue.setName("JPQL Arena");
        venue.setCity("Santa Marta");
        venue.setAddress("Carrera 5 #5-5");
        venue.setCapacity(1000);
        venue.setActive(true);

        venue = venueRepository.save(venue);

        Event event = new Event();
        event.setEventCode("EVT-JPQL");
        event.setName("JPQL Event");
        event.setDescription("Evento JPQL");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.of(2026, 10, 20, 19, 0));
        event.setMinimumAge(18);
        event.setVenue(venue);

        event = eventRepository.save(event);

        User user = new User();
        user.setUsername("natalia.jpql");
        user.setEmail("natalia.jpql@example.com");
        user.setActive(true);

        user = userRepository.save(user);

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

    @Test
    void shouldFindPaidTicketsByEventCode() {
        Venue venue = new Venue();
        venue.setCode("VEN-EVENTCODE");
        venue.setName("Event Code Arena");
        venue.setCity("Santa Marta");
        venue.setAddress("Carrera 6 #6-6");
        venue.setCapacity(1000);
        venue.setActive(true);

        venue = venueRepository.save(venue);

        Event event = new Event();
        event.setEventCode("EVT-EVENTCODE");
        event.setName("Event Code Event");
        event.setDescription("Evento por código");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.of(2026, 10, 15, 19, 0));
        event.setMinimumAge(18);
        event.setVenue(venue);

        event = eventRepository.save(event);

        User user = new User();
        user.setUsername("natalia.eventcode");
        user.setEmail("natalia.eventcode@example.com");
        user.setActive(true);

        user = userRepository.save(user);

        Ticket ticket = new Ticket();
        ticket.setTicketCode("TCK-EVENTCODE-001");
        ticket.setType(TicketType.VIP);
        ticket.setPrice(new BigDecimal("250000.00"));
        ticket.setStatus(TicketStatus.PAID);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);

        ticketRepository.save(ticket);

        var tickets = ticketRepository.findByEvent_EventCodeAndStatus(
                "EVT-EVENTCODE",
                TicketStatus.PAID
        );

        assertEquals(1, tickets.size());
        assertEquals(
                "TCK-EVENTCODE-001",
                tickets.get(0).getTicketCode()
        );
    }

    @Test
    @Transactional
    void shouldFindRecommendedEventsByDateCityAndArtistText() {
        Venue venue = new Venue();
        venue.setCode("VEN-RECOMMENDED");
        venue.setName("Recommended Arena");
        venue.setCity("Santa Marta");
        venue.setAddress("Carrera 7 #7-7");
        venue.setCapacity(3000);
        venue.setActive(true);

        venue = venueRepository.save(venue);

        Artist artist = artistRepository.findByStageName("Solar Beat")
                .orElseThrow();

        Event event = new Event();
        event.setEventCode("EVT-RECOMMENDED");
        event.setName("Solar Event");
        event.setDescription("Evento recomendado");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.of(2026, 11, 20, 19, 0));
        event.setMinimumAge(18);
        event.setVenue(venue);

        event = eventRepository.save(event);

        entityManager.createNativeQuery(
                "INSERT INTO event_artists (event_id, artist_id) " +
                "VALUES (:eventId, :artistId)"
        )
        .setParameter("eventId", event.getId())
        .setParameter("artistId", artist.getId())
        .executeUpdate();

        var events = eventRepository.findRecommendedEvents(
                LocalDateTime.of(2026, 10, 1, 0, 0),
                "Santa Marta",
                "solar"
        );

        assertEquals(1, events.size());
        assertEquals(
                "EVT-RECOMMENDED",
                events.get(0).getEventCode()
        );
    }

    @Test
    void shouldFindTicketsByEventDateAfterOrderedByEventDate() {
        Venue venue = new Venue();
        venue.setCode("VEN-FUTURE");
        venue.setName("Future Arena");
        venue.setCity("Santa Marta");
        venue.setAddress("Carrera 8 #8-8");
        venue.setCapacity(1000);
        venue.setActive(true);

        venue = venueRepository.save(venue);

        Event event = new Event();
        event.setEventCode("EVT-FUTURE");
        event.setName("Future Event");
        event.setDescription("Evento futuro");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.of(2026, 12, 15, 19, 0));
        event.setMinimumAge(18);
        event.setVenue(venue);

        event = eventRepository.save(event);

        User user = new User();
        user.setUsername("natalia.future");
        user.setEmail("natalia.future@example.com");
        user.setActive(true);

        user = userRepository.save(user);

        Ticket ticket = new Ticket();
        ticket.setTicketCode("TCK-FUTURE-001");
        ticket.setType(TicketType.GENERAL);
        ticket.setPrice(new BigDecimal("100000.00"));
        ticket.setStatus(TicketStatus.PAID);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);

        ticketRepository.save(ticket);

        var tickets = ticketRepository
                .findByEvent_EventDateAfterOrderByEvent_EventDateAsc(
                        LocalDateTime.of(2026, 11, 1, 0, 0)
                );

        assertEquals(1, tickets.size());
        assertEquals(
                "TCK-FUTURE-001",
                tickets.get(0).getTicketCode()
        );
    }

    @Test
    @Transactional
    void shouldFindEventsByArtist() {
        Venue venue = new Venue();
        venue.setCode("VEN-ARTIST");
        venue.setName("Artist Arena");
        venue.setCity("Santa Marta");
        venue.setAddress("Carrera 9 #9-9");
        venue.setCapacity(2000);
        venue.setActive(true);

        venue = venueRepository.save(venue);

        Artist artist = artistRepository.findByStageName("Solar Beat")
                .orElseThrow();

        Event event = new Event();
        event.setEventCode("EVT-ARTIST");
        event.setName("Artist Event");
        event.setDescription("Evento por artista");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.of(2026, 11, 10, 19, 0));
        event.setMinimumAge(18);
        event.setVenue(venue);

        event = eventRepository.save(event);

        entityManager.createNativeQuery(
                "INSERT INTO event_artists (event_id, artist_id) " +
                "VALUES (:eventId, :artistId)"
        )
        .setParameter("eventId", event.getId())
        .setParameter("artistId", artist.getId())
        .executeUpdate();

        var events = eventRepository.findEventsByArtist("Solar Beat");

        assertEquals(1, events.size());
        assertEquals(
                "EVT-ARTIST",
                events.get(0).getEventCode()
        );
    }
    @Test
void shouldRejectDuplicateVenueCode() {
    Venue firstVenue = new Venue();
    firstVenue.setCode("VEN-UNIQUE");
    firstVenue.setName("Unique Arena");
    firstVenue.setCity("Santa Marta");
    firstVenue.setAddress("Carrera 10 #10-10");
    firstVenue.setCapacity(1000);
    firstVenue.setActive(true);

    venueRepository.saveAndFlush(firstVenue);

    Venue duplicateVenue = new Venue();
    duplicateVenue.setCode("VEN-UNIQUE");
    duplicateVenue.setName("Another Arena");
    duplicateVenue.setCity("Santa Marta");
    duplicateVenue.setAddress("Carrera 11 #11-11");
    duplicateVenue.setCapacity(2000);
    duplicateVenue.setActive(true);

    org.junit.jupiter.api.Assertions.assertThrows(
            Exception.class,
            () -> venueRepository.saveAndFlush(duplicateVenue)
    );
}
}