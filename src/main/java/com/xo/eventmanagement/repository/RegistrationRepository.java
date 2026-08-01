package com.xo.eventmanagement.repository;

import com.xo.eventmanagement.entity.Event;
import com.xo.eventmanagement.entity.Registration;
import com.xo.eventmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUser(User user);
    List<Registration> findByEvent(Event event);
    Optional<Registration> findByEventAndUser(Event event, User user);
    long countByEvent(Event event);
    void deleteByEvent(Event event);
    void deleteByUser(User user);
}
