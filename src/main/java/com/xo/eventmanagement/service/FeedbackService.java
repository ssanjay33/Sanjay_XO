package com.xo.eventmanagement.service;

import com.xo.eventmanagement.dto.FeedbackRequest;
import com.xo.eventmanagement.entity.Event;
import com.xo.eventmanagement.entity.Feedback;
import com.xo.eventmanagement.entity.User;
import com.xo.eventmanagement.exception.ResourceNotFoundException;
import com.xo.eventmanagement.repository.EventRepository;
import com.xo.eventmanagement.repository.FeedbackRepository;
import com.xo.eventmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    public Feedback addFeedback(FeedbackRequest request, Long userId) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Feedback feedback = new Feedback();
        feedback.setEvent(event);
        feedback.setUser(user);
        feedback.setRating(request.getRating());
        feedback.setComments(request.getComments());

        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getFeedbackForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return feedbackRepository.findByEvent(event);
    }
}
