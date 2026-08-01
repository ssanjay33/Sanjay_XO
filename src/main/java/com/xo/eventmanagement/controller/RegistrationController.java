package com.xo.eventmanagement.controller;

import com.xo.eventmanagement.dto.ApiResponse;
import com.xo.eventmanagement.dto.RegistrationRequest;
import com.xo.eventmanagement.entity.Registration;
import com.xo.eventmanagement.security.UserPrincipal;
import com.xo.eventmanagement.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<Registration> register(@Valid @RequestBody RegistrationRequest request,
                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        Registration registration = registrationService.registerForEvent(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(registration);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<Registration>> myBookings(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(registrationService.getRegistrationsByUser(currentUser.getId()));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Registration>> byEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.getRegistrationsByEvent(eventId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> cancel(@PathVariable Long id,
                                               @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        registrationService.cancelRegistration(id, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(new ApiResponse(true, "Registration cancelled successfully"));
    }
}
