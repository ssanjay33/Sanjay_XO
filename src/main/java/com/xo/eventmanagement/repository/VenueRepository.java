package com.xo.eventmanagement.repository;

import com.xo.eventmanagement.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
