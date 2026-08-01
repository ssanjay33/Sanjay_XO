package com.xo.eventmanagement.controller;

import com.xo.eventmanagement.dto.FeedbackRequest;
import com.xo.eventmanagement.entity.Feedback;
import com.xo.eventmanagement.security.UserPrincipal;
import com.xo.eventmanagement.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<Feedback> addFeedback(@Valid @RequestBody FeedbackRequest request,
                                                 @AuthenticationPrincipal UserPrincipal currentUser) {
        Feedback feedback = feedbackService.addFeedback(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(feedback);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Feedback>> getFeedback(@PathVariable Long eventId) {
        return ResponseEntity.ok(feedbackService.getFeedbackForEvent(eventId));
    }
}
