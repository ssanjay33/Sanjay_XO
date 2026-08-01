package com.xo.eventmanagement.repository;

import com.xo.eventmanagement.entity.Event;
import com.xo.eventmanagement.entity.Feedback;
import com.xo.eventmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByEvent(Event event);
    void deleteByEvent(Event event);
    void deleteByUser(User user);
}
