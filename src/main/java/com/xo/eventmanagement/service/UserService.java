package com.xo.eventmanagement.service;

import com.xo.eventmanagement.entity.Event;
import com.xo.eventmanagement.entity.User;
import com.xo.eventmanagement.exception.ResourceNotFoundException;
import com.xo.eventmanagement.repository.EventRepository;
import com.xo.eventmanagement.repository.FeedbackRepository;
import com.xo.eventmanagement.repository.RegistrationRepository;
import com.xo.eventmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User updateUserRole(Long id, User.Role role) {
        User user = getUserById(id);
        user.setRole(role);
        return userRepository.save(user);
    }

    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    /**
     * Deletes a user along with everything that references them:
     * their feedback, their bookings (as an attendee), and - if they are
     * an organizer - every event they created (which itself cascades that
     * event's own registrations/feedback). This ensures admin delete never
     * gets stuck on a foreign-key constraint.
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);

        feedbackRepository.deleteByUser(user);
        registrationRepository.deleteByUser(user);

        List<Event> organizedEvents = eventRepository.findByOrganizer(user);
        for (Event event : organizedEvents) {
            feedbackRepository.deleteByEvent(event);
            registrationRepository.deleteByEvent(event);
            eventRepository.delete(event);
        }

        userRepository.delete(user);
    }
}
