package com.xo.eventmanagement.controller;

import com.xo.eventmanagement.dto.ApiResponse;
import com.xo.eventmanagement.entity.Venue;
import com.xo.eventmanagement.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @GetMapping
    public ResponseEntity<List<Venue>> getAll() {
        return ResponseEntity.ok(venueService.getAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @PostMapping
    public ResponseEntity<Venue> create(@Valid @RequestBody Venue venue) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.create(venue));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Venue> update(@PathVariable Long id, @Valid @RequestBody Venue venue) {
        return ResponseEntity.ok(venueService.update(id, venue));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        venueService.delete(id);
        return ResponseEntity.ok(new ApiResponse(true, "Venue deleted successfully"));
    }
}
