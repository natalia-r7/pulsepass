package com.pulsepass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.pulsepass.entity.UserProfile;
import com.pulsepass.entity.Venue;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserProfileRepository;
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

    @Autowired
private UserProfileRepository userProfileRepository;

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

    Event firstEvent = new Event();
    firstEvent.setEventCode("EVT-ARTIST-001");
    firstEvent.setName("Solar Beat Event 1");
    firstEvent.setDescription("Primer evento de Solar Beat");
    firstEvent.setCategory(EventCategory.MUSIC);
    firstEvent.setStatus(EventStatus.PUBLISHED);
    firstEvent.setEventDate(LocalDateTime.of(2026, 11, 10, 19, 0));
    firstEvent.setMinimumAge(18);
    firstEvent.setVenue(venue);
    firstEvent.getArtists().add(artist);

    Event secondEvent = new Event();
    secondEvent.setEventCode("EVT-ARTIST-002");
    secondEvent.setName("Solar Beat Event 2");
    secondEvent.setDescription("Segundo evento de Solar Beat");
    secondEvent.setCategory(EventCategory.MUSIC);
    secondEvent.setStatus(EventStatus.PUBLISHED);
    secondEvent.setEventDate(LocalDateTime.of(2026, 11, 20, 19, 0));
    secondEvent.setMinimumAge(18);
    secondEvent.setVenue(venue);
    secondEvent.getArtists().add(artist);

    eventRepository.save(firstEvent);
    eventRepository.saveAndFlush(secondEvent);

    var events = eventRepository.findEventsByArtist("Solar Beat");

    assertEquals(2, events.size());
    assertTrue(events.stream()
            .anyMatch(event -> event.getEventCode().equals("EVT-ARTIST-001")));
    assertTrue(events.stream()
            .anyMatch(event -> event.getEventCode().equals("EVT-ARTIST-002")));
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
@Test
void shouldRejectDuplicateTicketCode() {
    Venue venue = new Venue();
    venue.setCode("VEN-TICKET-UNIQUE");
    venue.setName("Ticket Unique Arena");
    venue.setCity("Santa Marta");
    venue.setAddress("Carrera 15 #15-15");
    venue.setCapacity(2000);
    venue.setActive(true);
    venue = venueRepository.save(venue);

    Event event = new Event();
    event.setEventCode("EVT-TICKET-UNIQUE");
    event.setName("Ticket Unique Event");
    event.setCategory(EventCategory.MUSIC);
    event.setStatus(EventStatus.PUBLISHED);
    event.setEventDate(LocalDateTime.of(2026, 12, 30, 19, 0));
    event.setMinimumAge(18);
    event.setVenue(venue);
    event = eventRepository.save(event);

    User user = new User();
    user.setUsername("natalia.ticketunique");
    user.setEmail("natalia.ticketunique@example.com");
    user.setActive(true);
    user = userRepository.save(user);

    Ticket firstTicket = new Ticket();
    firstTicket.setTicketCode("TCK-DUPLICATE-001");
    firstTicket.setType(TicketType.VIP);
    firstTicket.setPrice(new BigDecimal("250000.00"));
    firstTicket.setStatus(TicketStatus.PAID);
    firstTicket.setPurchaseDate(LocalDateTime.now());
    firstTicket.setUser(user);
    firstTicket.setEvent(event);

    ticketRepository.saveAndFlush(firstTicket);

    Ticket duplicateTicket = new Ticket();
    duplicateTicket.setTicketCode("TCK-DUPLICATE-001");
    duplicateTicket.setType(TicketType.GENERAL);
    duplicateTicket.setPrice(new BigDecimal("120000.00"));
    duplicateTicket.setStatus(TicketStatus.RESERVED);
    duplicateTicket.setPurchaseDate(LocalDateTime.now());
    duplicateTicket.setUser(user);
    duplicateTicket.setEvent(event);

    org.junit.jupiter.api.Assertions.assertThrows(
        Exception.class,
        () -> ticketRepository.saveAndFlush(duplicateTicket)
    );
}
    @Test
    void shouldAllowVenueToHaveMultipleEvents() {
        Venue venue = new Venue();
        venue.setCode("VEN-MULTIPLE");
        venue.setName("Multiple Events Arena");
        venue.setCity("Santa Marta");
        venue.setAddress("Carrera 12 #12-12");
        venue.setCapacity(3000);
        venue.setActive(true);

        venue = venueRepository.save(venue);

        Event firstEvent = new Event();
        firstEvent.setEventCode("EVT-MULTIPLE-001");
        firstEvent.setName("First Event");
        firstEvent.setDescription("Primer evento");
        firstEvent.setCategory(EventCategory.MUSIC);
        firstEvent.setStatus(EventStatus.PUBLISHED);
        firstEvent.setEventDate(LocalDateTime.of(2026, 11, 20, 19, 0));
        firstEvent.setMinimumAge(18);
        firstEvent.setVenue(venue);

        Event secondEvent = new Event();
        secondEvent.setEventCode("EVT-MULTIPLE-002");
        secondEvent.setName("Second Event");
        secondEvent.setDescription("Segundo evento");
        secondEvent.setCategory(EventCategory.CULTURE);
        secondEvent.setStatus(EventStatus.PUBLISHED);
        secondEvent.setEventDate(LocalDateTime.of(2026, 11, 25, 19, 0));
        secondEvent.setMinimumAge(18);
        secondEvent.setVenue(venue);

        eventRepository.save(firstEvent);
        eventRepository.save(secondEvent);

        var events = eventRepository.findByVenue_Code("VEN-MULTIPLE");

        assertEquals(2, events.size());
    }
    @Test
void shouldAllowOnlyOneProfilePerUser() {
    User user = new User();
    user.setUsername("natalia.profile");
    user.setEmail("natalia.profile@example.com");
    user.setActive(true);
    user = userRepository.save(user);

    UserProfile firstProfile = new UserProfile();
    firstProfile.setFirstName("Natalia");
    firstProfile.setLastName("Ramirez");
    firstProfile.setCity("Santa Marta");
    firstProfile.setUser(user);

    firstProfile = userProfileRepository.saveAndFlush(firstProfile);

    var foundProfile = userProfileRepository.findById(firstProfile.getId());

    assertEquals(
        user.getId(),
        foundProfile.orElseThrow().getUser().getId()
    );

    UserProfile secondProfile = new UserProfile();
    secondProfile.setFirstName("Otro");
    secondProfile.setLastName("Perfil");
    secondProfile.setCity("Santa Marta");
    secondProfile.setUser(user);

    org.junit.jupiter.api.Assertions.assertThrows(
        Exception.class,
        () -> userProfileRepository.saveAndFlush(secondProfile)
    );
}
@Test
@Transactional
void shouldAllowEventToHaveMultipleArtists() {
    Venue venue = new Venue();
    venue.setCode("VEN-NM-001");
    venue.setName("N M Arena");
    venue.setCity("Santa Marta");
    venue.setAddress("Carrera 13 #13-13");
    venue.setCapacity(3000);
    venue.setActive(true);
    venue = venueRepository.save(venue);

    Artist firstArtist = artistRepository
            .findByStageName("Solar Beat")
            .orElseThrow();

    Artist secondArtist = artistRepository
            .findByStageName("Neon Waves")
            .orElseThrow();

    Event event = new Event();
    event.setEventCode("EVT-NM-001");
    event.setName("N M Event");
    event.setDescription("Evento con varios artistas");
    event.setCategory(EventCategory.MUSIC);
    event.setStatus(EventStatus.PUBLISHED);
    event.setEventDate(LocalDateTime.of(2026, 12, 20, 19, 0));
    event.setMinimumAge(18);
    event.setVenue(venue);

    event.getArtists().add(firstArtist);
    event.getArtists().add(secondArtist);

    event = eventRepository.saveAndFlush(event);

    var foundEvent = eventRepository.findById(event.getId()).orElseThrow();

    assertEquals(2, foundEvent.getArtists().size());
    assertEquals("Solar Beat", foundEvent.getArtists().get(0).getStageName());
    assertEquals("Neon Waves", foundEvent.getArtists().get(1).getStageName());
}
@Test
void shouldAssociateTicketWithUserAndEvent() {
    Venue venue = new Venue();
    venue.setCode("VEN-TICKET-REL");
    venue.setName("Ticket Relation Arena");
    venue.setCity("Santa Marta");
    venue.setAddress("Carrera 14 #14-14");
    venue.setCapacity(2000);
    venue.setActive(true);
    venue = venueRepository.save(venue);

    Event event = new Event();
    event.setEventCode("EVT-TICKET-REL");
    event.setName("Ticket Relation Event");
    event.setDescription("Evento para probar relaciones");
    event.setCategory(EventCategory.MUSIC);
    event.setStatus(EventStatus.PUBLISHED);
    event.setEventDate(LocalDateTime.of(2026, 12, 25, 19, 0));
    event.setMinimumAge(18);
    event.setVenue(venue);
    event = eventRepository.save(event);

    User user = new User();
    user.setUsername("natalia.ticketrel");
    user.setEmail("natalia.ticketrel@example.com");
    user.setActive(true);
    user = userRepository.save(user);

    Ticket ticket = new Ticket();
    ticket.setTicketCode("TCK-REL-001");
    ticket.setType(TicketType.VIP);
    ticket.setPrice(new BigDecimal("250000.00"));
    ticket.setStatus(TicketStatus.PAID);
    ticket.setPurchaseDate(LocalDateTime.now());
    ticket.setUser(user);
    ticket.setEvent(event);

    ticket = ticketRepository.saveAndFlush(ticket);

    var foundTicket = ticketRepository.findById(ticket.getId()).orElseThrow();

    assertEquals(user.getId(), foundTicket.getUser().getId());
    assertEquals(event.getId(), foundTicket.getEvent().getId());
}
@Test
void shouldFindUserByEmailIgnoringCase() {
    User user = new User();
    user.setUsername("natalia.query");
    user.setEmail("natalia.query@example.com");
    user.setActive(true);
    userRepository.save(user);

    var foundUser = userRepository.findByEmailIgnoreCase(
        "NATALIA.QUERY@EXAMPLE.COM"
    );

    assertEquals(
        "natalia.query@example.com",
        foundUser.orElseThrow().getEmail()
    );
}
@Test
void shouldFindTicketsByUserEmail() {
    User user = new User();
    user.setUsername("natalia.email");
    user.setEmail("natalia.email@example.com");
    user.setActive(true);
    user = userRepository.save(user);

    Venue venue = new Venue();
    venue.setCode("VEN-EMAIL-001");
    venue.setName("Email Arena");
    venue.setCity("Santa Marta");
    venue.setAddress("Carrera 1 #1-01");
    venue.setCapacity(1000);
    venue.setActive(true);
    venue = venueRepository.save(venue);

    Event event = new Event();
    event.setEventCode("EVT-EMAIL-001");
    event.setName("Email Event");
    event.setDescription("Evento para probar búsqueda por email");
    event.setCategory(EventCategory.MUSIC);
    event.setStatus(EventStatus.PUBLISHED);
    event.setEventDate(LocalDateTime.of(2026, 12, 30, 19, 0));
    event.setMinimumAge(18);
    event.setVenue(venue);
    event = eventRepository.save(event);

    Ticket ticket = new Ticket();
    ticket.setTicketCode("TCK-EMAIL-001");
    ticket.setType(TicketType.GENERAL);
    ticket.setPrice(new BigDecimal("120000.00"));
    ticket.setStatus(TicketStatus.PAID);
    ticket.setPurchaseDate(LocalDateTime.now());
    ticket.setUser(user);
    ticket.setEvent(event);

    ticketRepository.saveAndFlush(ticket);

    var tickets = ticketRepository.findByUser_Email(
        "natalia.email@example.com"
    );

    assertEquals(1, tickets.size());
    assertEquals("TCK-EMAIL-001", tickets.get(0).getTicketCode());
}
@Test
void shouldFindTicketByTicketCode() {
    User user = new User();
    user.setUsername("natalia.ticketcode");
    user.setEmail("natalia.ticketcode@example.com");
    user.setActive(true);
    user = userRepository.save(user);

    Venue venue = new Venue();
    venue.setCode("VEN-CODE-001");
    venue.setName("Code Arena");
    venue.setCity("Santa Marta");
    venue.setAddress("Carrera 2 #2-02");
    venue.setCapacity(1000);
    venue.setActive(true);
    venue = venueRepository.save(venue);

    Event event = new Event();
    event.setEventCode("EVT-CODE-001");
    event.setName("Code Event");
    event.setCategory(EventCategory.TECHNOLOGY);
    event.setStatus(EventStatus.PUBLISHED);
    event.setEventDate(LocalDateTime.of(2026, 12, 28, 19, 0));
    event.setMinimumAge(18);
    event.setVenue(venue);
    event = eventRepository.save(event);

    Ticket ticket = new Ticket();
    ticket.setTicketCode("TCK-CODE-001");
    ticket.setType(TicketType.VIP);
    ticket.setPrice(new BigDecimal("250000.00"));
    ticket.setStatus(TicketStatus.PAID);
    ticket.setPurchaseDate(LocalDateTime.now());
    ticket.setUser(user);
    ticket.setEvent(event);

    ticketRepository.saveAndFlush(ticket);

    var foundTicket = ticketRepository.findByTicketCode("TCK-CODE-001");

    assertEquals(
        "TCK-CODE-001",
        foundTicket.orElseThrow().getTicketCode()
    );
}
@Test
void shouldCountOnlyPaidTicketsByEvent() {
    Venue venue = new Venue();
    venue.setCode("VEN-COUNT-001");
    venue.setName("Count Arena");
    venue.setCity("Santa Marta");
    venue.setAddress("Carrera 3 #3-03");
    venue.setCapacity(2000);
    venue.setActive(true);
    venue = venueRepository.save(venue);

    Event event = new Event();
    event.setEventCode("EVT-COUNT-001");
    event.setName("Count Event");
    event.setCategory(EventCategory.MUSIC);
    event.setStatus(EventStatus.PUBLISHED);
    event.setEventDate(LocalDateTime.of(2026, 12, 29, 19, 0));
    event.setMinimumAge(18);
    event.setVenue(venue);
    event = eventRepository.save(event);

    User user = new User();
    user.setUsername("natalia.count");
    user.setEmail("natalia.count@example.com");
    user.setActive(true);
    user = userRepository.save(user);

    Ticket paidTicket = new Ticket();
    paidTicket.setTicketCode("TCK-COUNT-PAID");
    paidTicket.setType(TicketType.GENERAL);
    paidTicket.setPrice(new BigDecimal("120000.00"));
    paidTicket.setStatus(TicketStatus.PAID);
    paidTicket.setPurchaseDate(LocalDateTime.now());
    paidTicket.setUser(user);
    paidTicket.setEvent(event);
    ticketRepository.save(paidTicket);

    Ticket reservedTicket = new Ticket();
    reservedTicket.setTicketCode("TCK-COUNT-RESERVED");
    reservedTicket.setType(TicketType.GENERAL);
    reservedTicket.setPrice(new BigDecimal("120000.00"));
    reservedTicket.setStatus(TicketStatus.RESERVED);
    reservedTicket.setPurchaseDate(LocalDateTime.now());
    reservedTicket.setUser(user);
    reservedTicket.setEvent(event);
    ticketRepository.saveAndFlush(reservedTicket);

    long paidTickets = eventRepository.countPaidTicketsByEvent(event.getId());

    assertEquals(1, paidTickets);
}
@Test
void shouldFindEventsByCityAndArtist() {
    Venue venue = new Venue();
    venue.setCode("VEN-JOIN-001");
    venue.setName("Join Arena");
    venue.setCity("Santa Marta");
    venue.setAddress("Carrera 4 #4-04");
    venue.setCapacity(2000);
    venue.setActive(true);
    venue = venueRepository.save(venue);

    Artist artist = artistRepository
            .findByStageName("Solar Beat")
            .orElseThrow();

    Event event = new Event();
    event.setEventCode("EVT-JOIN-001");
    event.setName("Join Event");
    event.setCategory(EventCategory.MUSIC);
    event.setStatus(EventStatus.PUBLISHED);
    event.setEventDate(LocalDateTime.of(2026, 12, 27, 19, 0));
    event.setMinimumAge(18);
    event.setVenue(venue);

    event.getArtists().add(artist);

    event = eventRepository.saveAndFlush(event);

    var events = eventRepository.findEventsByCityAndArtist(
        "Santa Marta",
        "Solar Beat"
    );

    assertEquals(1, events.size());
    assertEquals("EVT-JOIN-001", events.get(0).getEventCode());
}
}