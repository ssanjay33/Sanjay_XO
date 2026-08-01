package com.xo.eventmanagement.service;

import com.xo.eventmanagement.dto.EventRequest;
import com.xo.eventmanagement.entity.Category;
import com.xo.eventmanagement.entity.Event;
import com.xo.eventmanagement.entity.User;
import com.xo.eventmanagement.entity.Venue;
import com.xo.eventmanagement.exception.BadRequestException;
import com.xo.eventmanagement.exception.ResourceNotFoundException;
import com.xo.eventmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getUpcomingEvents() {
        return eventRepository.findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate.now());
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    public List<Event> getEventsByOrganizer(Long organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));
        return eventRepository.findByOrganizer(organizer);
    }

    public List<Event> getEventsByCategory(Long categoryId) {
        return eventRepository.findByCategoryId(categoryId);
    }

    public List<Event> searchEvents(String keyword) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword);
    }

    /**
     * Prevents double-booking a venue: if another (non-cancelled) event already
     * occupies this venue on this date with an overlapping time range, reject it.
     * If either event has no start/end time specified, the whole day is treated
     * as blocked for that venue (safest default).
     */
    private void checkVenueAvailability(Venue venue, LocalDate date, LocalTime startTime,
                                         LocalTime endTime, Long excludeEventId) {
        List<Event> sameDayEvents = eventRepository.findByVenueIdAndEventDate(venue.getId(), date);

        for (Event existing : sameDayEvents) {
            if (excludeEventId != null && existing.getId().equals(excludeEventId)) {
                continue; // skip the event being edited
            }
            if (existing.getStatus() == Event.EventStatus.CANCELLED) {
                continue; // cancelled events don't block the slot
            }

            boolean overlaps;
            if (startTime == null || endTime == null || existing.getStartTime() == null || existing.getEndTime() == null) {
                // Missing time info on either side - be safe and treat as a full-day conflict
                overlaps = true;
            } else {
                overlaps = startTime.isBefore(existing.getEndTime()) && existing.getStartTime().isBefore(endTime);
            }

            if (overlaps) {
                String timeInfo = (existing.getStartTime() != null && existing.getEndTime() != null)
                        ? existing.getStartTime() + " - " + existing.getEndTime()
                        : "that day";
                throw new BadRequestException(
                        "\"" + venue.getName() + "\" is already booked for \"" + existing.getTitle() +
                        "\" on " + date + " (" + timeInfo + "). Please choose a different time or venue."
                );
            }
        }
    }

    public Event createEvent(EventRequest request, Long organizerId) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        if (request.getEventDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Event date cannot be in the past");
        }

        checkVenueAvailability(venue, request.getEventDate(), request.getStartTime(), request.getEndTime(), null);

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(category);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setEventDate(request.getEventDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setPrice(request.getPrice() != null ? request.getPrice() : java.math.BigDecimal.ZERO);
        event.setTotalSeats(request.getTotalSeats());
        event.setAvailableSeats(request.getTotalSeats());
        event.setImageUrl(request.getImageUrl());
        event.setStatus(Event.EventStatus.UPCOMING);

        return eventRepository.save(event);
    }

    public Event updateEvent(Long eventId, EventRequest request, Long requesterId, boolean isAdmin) {
        Event event = getEventById(eventId);

        if (!isAdmin && !event.getOrganizer().getId().equals(requesterId)) {
            throw new BadRequestException("You are not authorized to update this event");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        checkVenueAvailability(venue, request.getEventDate(), request.getStartTime(), request.getEndTime(), eventId);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(category);
        event.setVenue(venue);
        event.setEventDate(request.getEventDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setPrice(request.getPrice());
        event.setImageUrl(request.getImageUrl());

        if (request.getTotalSeats() != null) {
            int bookedSeats = event.getTotalSeats() - event.getAvailableSeats();
            event.setTotalSeats(request.getTotalSeats());
            event.setAvailableSeats(Math.max(0, request.getTotalSeats() - bookedSeats));
        }

        return eventRepository.save(event);
    }

    /**
     * Deletes an event along with every registration and feedback that
     * references it, so the delete never fails with a foreign-key error.
     */
    @Transactional
    public void deleteEvent(Long eventId, Long requesterId, boolean isAdmin) {
        Event event = getEventById(eventId);
        if (!isAdmin && !event.getOrganizer().getId().equals(requesterId)) {
            throw new BadRequestException("You are not authorized to delete this event");
        }
        feedbackRepository.deleteByEvent(event);
        registrationRepository.deleteByEvent(event);
        eventRepository.delete(event);
    }

    public Event updateEventStatus(Long eventId, Event.EventStatus status) {
        Event event = getEventById(eventId);
        event.setStatus(status);
        return eventRepository.save(event);
    }
}
