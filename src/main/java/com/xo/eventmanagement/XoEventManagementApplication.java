package com.xo.eventmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XO EVENT MANAGEMENT SYSTEM
 * Main entry point of the Spring Boot Application.
 *
 * This application provides a complete Event Management platform where:
 *  - Admins can manage users, categories, venues and view reports
 *  - Organizers can create and manage their own events
 *  - Attendees can browse events, register/book tickets and give feedback
 */
@SpringBootApplication
public class XoEventManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(XoEventManagementApplication.class, args);
        System.out.println("=================================================");
        System.out.println(" XO EVENT MANAGEMENT SYSTEM STARTED SUCCESSFULLY ");
        System.out.println(" Visit: http://localhost:8080 ");
        System.out.println("=================================================");
    }
}
