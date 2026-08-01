package com.xo.eventmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @NotNull
    private LocalDate eventDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private BigDecimal price = BigDecimal.ZERO;

    private Integer totalSeats;

    private Integer availableSeats;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.UPCOMING;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum EventStatus {
        UPCOMING, ONGOING, COMPLETED, CANCELLED
    }
}
