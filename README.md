# XO Event Management System

A complete **full-stack Java** Event Management System built with **Spring Boot** (backend + REST API), **MySQL** (database), **Spring Security + JWT** (authentication), and **HTML/CSS/JavaScript** (frontend, served directly by Spring Boot).

Users can browse and book events. Organizers can create and manage their own events. Admins manage users, categories, venues, and view platform-wide stats.

---

## 1. Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2.5, Spring Data JPA (Hibernate), Spring Security |
| Auth | JWT (JSON Web Tokens) via `jjwt` |
| Database | MySQL 8 |
| Frontend | HTML5, CSS3, Vanilla JavaScript (fetch API) — served as static resources by Spring Boot |
| Build Tool | Maven |

---

## 2. Project Structure

```
xo-event-management/
├── pom.xml
├── database/
│   └── schema.sql                     (reference SQL script)
├── src/main/java/com/xo/eventmanagement/
│   ├── XoEventManagementApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java         (Spring Security + JWT filter chain)
│   │   └── DataSeeder.java             (seeds default admin + categories)
│   ├── entity/                         (User, Event, Category, Venue, Registration, Feedback)
│   ├── repository/                     (Spring Data JPA repositories)
│   ├── dto/                            (request/response DTOs)
│   ├── security/                       (JwtTokenProvider, JwtAuthenticationFilter, UserPrincipal, etc.)
│   ├── service/                        (business logic layer)
│   ├── controller/                     (REST controllers)
│   └── exception/                      (custom exceptions + global handler)
└── src/main/resources/
    ├── application.properties
    └── static/                         (frontend: HTML, css/, js/)
        ├── index.html                  (home page)
        ├── login.html
        ├── register.html
        ├── events.html                 (browse/search all events)
        ├── event-details.html          (view + book an event, leave feedback)
        ├── my-bookings.html            (attendee's bookings)
        ├── organizer-dashboard.html    (organizer: create/edit/delete own events)
        ├── admin-dashboard.html        (admin: users, categories, venues, all events, stats)
        ├── css/style.css
        └── js/common.js                (shared API/auth helper functions)
```

---

## 3. Features

**Attendee**
- Register/login, browse all events, search & filter by category
- View event details, book tickets (seat availability auto-updates)
- View "My Bookings", cancel a booking
- Leave a star rating + review on an event

**Organizer**
- Register/login as Organizer
- Create, edit, delete their own events
- View seat availability for their events

**Admin**
- Default seeded login: `admin@xoevents.com` / `Admin@123`
- Dashboard with total users / events / bookings stats
- Manage users (enable/disable, delete)
- Manage categories and venues (add/delete)
- View and delete any event on the platform

**Security**
- Passwords hashed with BCrypt
- Stateless JWT authentication (token stored in browser `localStorage`)
- Role-based access control (`ADMIN`, `ORGANIZER`, `ATTENDEE`) via Spring Security `@PreAuthorize`

---

## 4. Prerequisites

Install these before you begin:

1. **Java JDK 17+** — check with `java -version`
2. **Maven 3.8+** — check with `mvn -version` (or use the included `mvnw` wrapper if you add one)
3. **MySQL 8+** — running locally, with a root password you know
4. **An IDE** — IntelliJ IDEA / Eclipse / VS Code (optional but recommended)

---

## 5. Step-by-Step Setup Instructions

### Step 1 — Install MySQL and create the database
You don't have to manually create tables — Hibernate does that automatically — but you must have MySQL running.

```bash
# Log in to MySQL
mysql -u root -p

# (Optional) create the database manually — Spring Boot will also auto-create it
CREATE DATABASE IF NOT EXISTS xo_event_db;
EXIT;
```

### Step 2 — Configure database credentials
Open `src/main/resources/application.properties` and update these two lines with **your** MySQL username/password:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

### Step 3 — Extract / open the project
Unzip the project you downloaded, then open the `xo-event-management` folder in your IDE (IntelliJ: **File → Open**, select the folder with `pom.xml`).

### Step 4 — Build the project
From the project root folder, run:

```bash
mvn clean install
```

This downloads all dependencies (Spring Boot, Spring Security, MySQL driver, JWT libraries, Lombok, etc.) and compiles the project.

### Step 5 — Run the application
Option A — via Maven:
```bash
mvn spring-boot:run
```

Option B — via your IDE:
Right-click `XoEventManagementApplication.java` → **Run**.

Option C — build a runnable JAR and run it:
```bash
mvn clean package -DskipTests
java -jar target/xo-event-management.jar
```

On first successful run you should see in the console:
```
=================================================
 XO EVENT MANAGEMENT SYSTEM STARTED SUCCESSFULLY
 Visit: http://localhost:8080
=================================================
>> Default admin created: admin@xoevents.com / Admin@123
>> Default categories seeded
```

Hibernate will have auto-created all tables (`users`, `events`, `categories`, `venues`, `registrations`, `feedbacks`) in the `xo_event_db` database.

### Step 6 — Open the application
Go to **http://localhost:8080** in your browser. You'll land on the XO Event Management home page.

### Step 7 — Try it out
1. **As Admin:** Login with `admin@xoevents.com` / `Admin@123` → go to Admin Panel → add a Venue (e.g. "Palace Grounds", Bengaluru, capacity 500) since events require a venue and category (categories are already seeded).
2. **As Organizer:** Click Sign Up → register with role "Organizer" → login → go to "My Events" → Create Event (pick the venue/category you just saw, set date/price/seats).
3. **As Attendee:** Sign up with role "Attendee" (or just browse without logging in) → go to Events → open an event → Book Now → confirm tickets → check "My Bookings".
4. Leave a review on the event details page.

---

## 6. REST API Reference

Base URL: `http://localhost:8080/api`

### Auth (public)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/signup` | Register new user (`ATTENDEE` or `ORGANIZER`) |
| POST | `/auth/login` | Login → returns JWT token |

### Events
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/events` | Public | List all events |
| GET | `/events/upcoming` | Public | List upcoming events |
| GET | `/events/{id}` | Public | Get event details |
| GET | `/events/search?keyword=` | Public | Search events by title |
| GET | `/events/category/{categoryId}` | Public | Filter by category |
| GET | `/events/organizer/{organizerId}` | Public | Events by organizer |
| POST | `/events` | Organizer/Admin | Create event |
| PUT | `/events/{id}` | Organizer/Admin | Update event |
| DELETE | `/events/{id}` | Organizer/Admin | Delete event |
| PATCH | `/events/{id}/status?status=` | Organizer/Admin | Change event status |

### Categories & Venues
| Method | Endpoint | Auth |
|---|---|---|
| GET | `/categories` , `/venues` | Public |
| POST/PUT/DELETE | `/categories/**` | Admin |
| POST | `/venues` | Organizer/Admin |
| PUT/DELETE | `/venues/**` | Admin |

### Registrations (Bookings)
| Method | Endpoint | Auth |
|---|---|---|
| POST | `/registrations` | Logged-in user |
| GET | `/registrations/my-bookings` | Logged-in user |
| GET | `/registrations/event/{eventId}` | Logged-in user |
| DELETE | `/registrations/{id}` | Owner/Admin |

### Feedback
| Method | Endpoint | Auth |
|---|---|---|
| POST | `/feedback` | Logged-in user |
| GET | `/feedback/event/{eventId}` | Public |

### Users & Admin
| Method | Endpoint | Auth |
|---|---|---|
| GET | `/users/me` | Logged-in user |
| GET | `/users` | Admin |
| PATCH | `/users/{id}/role?role=` | Admin |
| PATCH | `/users/{id}/toggle-status` | Admin |
| DELETE | `/users/{id}` | Admin |
| GET | `/admin/dashboard/stats` | Admin |

**Authenticated requests** must include the header:
```
Authorization: Bearer <token-received-from-login>
```

---

## 7. How Authentication Works (JWT Flow)

1. User submits email/password on `login.html`.
2. `AuthController.login()` calls `AuthService`, which authenticates via Spring Security's `AuthenticationManager`.
3. On success, `JwtTokenProvider` generates a signed JWT containing the user's id, email, name, and role.
4. The frontend (`common.js` → `saveAuth()`) stores this token in `localStorage`.
5. Every subsequent API call attaches `Authorization: Bearer <token>` via `apiRequest()`.
6. On the backend, `JwtAuthenticationFilter` intercepts each request, validates the token, and sets the Spring Security context — enabling `@PreAuthorize` role checks on controllers.

---

## 8. Customizing / Extending

- **Change the app title/branding:** edit the `<title>` tags and `.brand` text in each HTML file, and `style.css` color variables (`--primary`, `--accent`) at the top of `css/style.css`.
- **Switch database:** update `spring.datasource.*` in `application.properties` (and add the relevant driver dependency to `pom.xml` if not MySQL).
- **Add image uploads for events:** the `Event` entity already has an `imageUrl` field — wire up a file upload controller and store the path there.
- **Add payment gateway integration:** hook into `RegistrationService.registerForEvent()` before marking `paymentStatus` as `PAID`.
- **Deploy:** package with `mvn clean package`, then deploy the JAR to any server with Java 17+ and a reachable MySQL instance (or containerize with Docker).

---

## 9. Troubleshooting

| Problem | Fix |
|---|---|
| `Communications link failure` on startup | MySQL isn't running, or wrong host/port in `application.properties` |
| `Access denied for user 'root'@'localhost'` | Wrong username/password in `application.properties` |
| Port 8080 already in use | Change `server.port` in `application.properties` |
| Frontend loads but events don't appear | Check the browser console; make sure the backend is running on the same port the frontend expects (8080) |
| 403 Forbidden on admin/organizer actions | You're not logged in with the right role, or your JWT expired — log in again |

---

## 10. Default Test Accounts

| Role | Email | Password |
|---|---|---|
| Admin | admin@xoevents.com | Admin@123 |
| Organizer / Attendee | *(create via Sign Up page)* | *(your choice)* |

---

Built with ❤️ — **XO Event Management System**
#   N E W _ E V E N T  
 #   S a n j a y _ X O  
 