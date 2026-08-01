package com.xo.eventmanagement.repository;

import com.xo.eventmanagement.entity.Event;
import com.xo.eventmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer(User organizer);
    List<Event> findByCategoryId(Long categoryId);
    List<Event> findByStatus(Event.EventStatus status);
    List<Event> findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate date);
    List<Event> findByTitleContainingIgnoreCase(String title);
    List<Event> findByVenueIdAndEventDate(Long venueId, LocalDate eventDate);
}
