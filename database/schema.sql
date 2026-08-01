-- ==========================================================
-- XO EVENT MANAGEMENT SYSTEM - DATABASE SCHEMA (REFERENCE)
-- ==========================================================
-- NOTE: You do NOT need to run this manually.
-- Spring Boot (Hibernate) auto-creates these tables on startup
-- because application.properties has:
--     spring.jpa.hibernate.ddl-auto=update
--
-- This script is provided for reference / manual setup if needed.
-- ==========================================================

CREATE DATABASE IF NOT EXISTS xo_event_db;
USE xo_event_db;

-- ---------------- USERS ----------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    phone VARCHAR(15),
    role ENUM('ADMIN','ORGANIZER','ATTENDEE') DEFAULT 'ATTENDEE',
    enabled BOOLEAN DEFAULT TRUE,
    created_at DATETIME
);

-- ---------------- CATEGORIES ----------------
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500)
);

-- ---------------- VENUES ----------------
CREATE TABLE IF NOT EXISTS venues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    capacity INT
);

-- ---------------- EVENTS ----------------
CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    category_id BIGINT,
    venue_id BIGINT,
    organizer_id BIGINT,
    event_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    price DECIMAL(10,2) DEFAULT 0,
    total_seats INT,
    available_seats INT,
    image_url VARCHAR(500),
    status ENUM('UPCOMING','ONGOING','COMPLETED','CANCELLED') DEFAULT 'UPCOMING',
    created_at DATETIME,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (venue_id) REFERENCES venues(id),
    FOREIGN KEY (organizer_id) REFERENCES users(id)
);

-- ---------------- REGISTRATIONS (BOOKINGS) ----------------
CREATE TABLE IF NOT EXISTS registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT,
    user_id BIGINT,
    number_of_tickets INT,
    total_amount DECIMAL(10,2),
    registration_date DATETIME,
    status ENUM('CONFIRMED','CANCELLED') DEFAULT 'CONFIRMED',
    payment_status ENUM('PENDING','PAID','REFUNDED') DEFAULT 'PAID',
    ticket_code VARCHAR(50),
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ---------------- FEEDBACK ----------------
CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT,
    user_id BIGINT,
    rating INT,
    comments VARCHAR(1000),
    created_at DATETIME,
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==========================================================
-- Default admin login (auto-seeded by the app on first run):
--   email: admin@xoevents.com
--   password: Admin@123
-- ==========================================================
