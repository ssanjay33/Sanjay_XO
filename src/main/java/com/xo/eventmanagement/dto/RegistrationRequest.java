package com.xo.eventmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistrationRequest {
    @NotNull
    private Long eventId;
    @NotNull
    private Integer numberOfTickets;
}
