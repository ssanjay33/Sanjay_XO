package com.xo.eventmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class EventRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private Long categoryId;
    @NotNull
    private Long venueId;
    @NotNull
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private Integer totalSeats;
    private String imageUrl;
}
