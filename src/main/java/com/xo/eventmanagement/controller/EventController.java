package com.xo.eventmanagement.controller;

import com.xo.eventmanagement.dto.ApiResponse;
import com.xo.eventmanagement.dto.EventRequest;
import com.xo.eventmanagement.entity.Event;
import com.xo.eventmanagement.security.UserPrincipal;
import com.xo.eventmanagement.service.EventService;
import com.xo.eventmanagement.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private FileStorageService fileStorageService;

    // Public endpoints - anyone can browse events
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents() {
        return ResponseEntity.ok(eventService.getUpcomingEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Event>> getEventsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(eventService.getEventsByCategory(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam String keyword) {
        return ResponseEntity.ok(eventService.searchEvents(keyword));
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<Event>> getEventsByOrganizer(@PathVariable Long organizerId) {
        return ResponseEntity.ok(eventService.getEventsByOrganizer(organizerId));
    }

    // Protected endpoints - only organizer/admin can manage events
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest request,
                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        Event event = eventService.createEvent(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id,
                                              @Valid @RequestBody EventRequest request,
                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Event event = eventService.updateEvent(id, request, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(event);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteEvent(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        eventService.deleteEvent(id, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(new ApiResponse(true, "Event deleted successfully"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Event> updateStatus(@PathVariable Long id, @RequestParam Event.EventStatus status) {
        return ResponseEntity.ok(eventService.updateEventStatus(id, status));
    }

    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.storeEventImage(file);
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", url);
        return ResponseEntity.ok(response);
    }
}
