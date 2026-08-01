package com.xo.eventmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(min = 6, max = 200)
    @JsonIgnore
    private String password;

    @Size(max = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ATTENDEE;

    private boolean enabled = true;

    private boolean emailVerified = false;

    @JsonIgnore
    private String verificationCode;

    @JsonIgnore
    private LocalDateTime verificationCodeExpiry;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        ADMIN, ORGANIZER, ATTENDEE
    }
}
