package com.xo.eventmanagement.service;

import com.xo.eventmanagement.dto.RegistrationRequest;
import com.xo.eventmanagement.entity.Event;
import com.xo.eventmanagement.entity.Registration;
import com.xo.eventmanagement.entity.User;
import com.xo.eventmanagement.exception.BadRequestException;
import com.xo.eventmanagement.exception.ResourceNotFoundException;
import com.xo.eventmanagement.repository.EventRepository;
import com.xo.eventmanagement.repository.RegistrationRepository;
import com.xo.eventmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Registration registerForEvent(RegistrationRequest request, Long userId) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (event.getStatus() == Event.EventStatus.CANCELLED) {
            throw new BadRequestException("Cannot register for a cancelled event");
        }

        int tickets = request.getNumberOfTickets();
        if (tickets <= 0) {
            throw new BadRequestException("Number of tickets must be at least 1");
        }
        if (event.getAvailableSeats() < tickets) {
            throw new BadRequestException("Not enough seats available. Only " + event.getAvailableSeats() + " left.");
        }

        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setNumberOfTickets(tickets);
        registration.setTotalAmount(event.getPrice().multiply(BigDecimal.valueOf(tickets)));
        registration.setTicketCode("XO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        registration.setStatus(Registration.RegistrationStatus.CONFIRMED);
        registration.setPaymentStatus(Registration.PaymentStatus.PAID);

        event.setAvailableSeats(event.getAvailableSeats() - tickets);
        eventRepository.save(event);

        return registrationRepository.save(registration);
    }

    public List<Registration> getRegistrationsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return registrationRepository.findByUser(user);
    }

    public List<Registration> getRegistrationsByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return registrationRepository.findByEvent(event);
    }

    @Transactional
    public void cancelRegistration(Long registrationId, Long userId, boolean isAdmin) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        if (!isAdmin && !registration.getUser().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to cancel this registration");
        }

        registration.setStatus(Registration.RegistrationStatus.CANCELLED);
        registration.setPaymentStatus(Registration.PaymentStatus.REFUNDED);

        Event event = registration.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + registration.getNumberOfTickets());
        eventRepository.save(event);

        registrationRepository.save(registration);
    }
}
