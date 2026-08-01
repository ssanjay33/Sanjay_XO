# What's New — Feature Update

This update adds five things you asked for. Here's what changed, and what you need to configure.

---

## 1. Event Images (Organizer Upload)

Organizers can now upload an image from their computer/gallery when creating or editing an event (JPG/PNG/WEBP/GIF, up to 5MB). It shows on the event card, the event details page, and the organizer's event table.

**⚠️ Important caveat for Render's free tier:** uploaded images are stored on the server's local disk. Render's free web service uses an **ephemeral filesystem** — meaning uploaded files are wiped every time the service restarts, sleeps, or redeploys. This is fine for local testing and demos, but for a real production app you'd want to switch to a persistent storage service like **Cloudinary**, **AWS S3**, or Render's paid **Persistent Disk** add-on. Ask me if you want help wiring one of those in later — it's a small change.

## 2. Venue Double-Booking Prevention

When creating or editing an event, the system now checks: is this venue already booked for an overlapping time on that date? If yes, it's rejected with a clear message, e.g.:

> "XO Hall" is already booked for "Tech Meetup" on 2026-08-15 (18:00 - 19:00). Please choose a different time or venue.

If either event doesn't have specific start/end times set, the whole day is treated as blocked for that venue (the safe default) — so it's worth always setting start/end times when creating events.

## 3. Admin Delete No Longer Gets Stuck

Previously, deleting a user or event could fail silently because of database foreign-key constraints (e.g., a user had bookings, or an event had registrations). Now:
- **Deleting an event** also deletes all its bookings and reviews.
- **Deleting a user** also deletes their bookings and reviews, and — if they're an organizer — every event they created (which cascades the same way).

This is destructive and irreversible, so the confirmation dialogs now say exactly what will be removed.

## 4. Colorful 3D Animated Theme

The whole visual design was rebuilt: animated gradient backgrounds on the navbar and hero, floating blob shapes, 3D tilt-on-hover event cards, glowing/pulsing buttons, staggered fade-in animations on page load, gradient-text pricing and headings. Same layout and pages, much more dynamic look and feel.

## 5. Email Verification with a Code

New signups now:
1. Submit the signup form
2. Get redirected to a **Verify Email** page
3. Receive a 6-digit code by email
4. Enter the code to activate their account
5. Only then can they log in

**⚠️ You must configure SMTP credentials for real emails to send.** Without this, the app still works — but codes are only written to the server logs (fine for testing, not for real users). See below.

---

## Required Configuration: Email (SMTP)

### For local development (`application.properties`)
Open `src/main/resources/application.properties` and fill in:

```properties
spring.mail.username=your.email@gmail.com
spring.mail.password=your-16-char-app-password
xo.app.mail.enabled=true
```

**To get a Gmail App Password:**
1. Go to your Google Account → Security → turn on **2-Step Verification** (required first)
2. Go to https://myaccount.google.com/apppasswords
3. Create a new App Password (choose "Mail" as the app)
4. Copy the 16-character password Google gives you — use that, **not** your normal Gmail password

If you leave `spring.mail.username` blank, the app automatically falls back to printing the code to the console/logs instead of failing — look for a line like:
```
>> [DEV MODE - EMAIL NOT CONFIGURED] Verification code for Sanjay (you@example.com): 483920
```

### For Render (production)
In your Render web service → **Environment** tab, add:

| Key | Value |
|---|---|
| `MAIL_ENABLED` | `true` |
| `MAIL_HOST` | `smtp.gmail.com` (or your provider's SMTP host) |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | your email address |
| `MAIL_PASSWORD` | your app password |

Without these, Render will do the same fallback — codes get written to the **Logs** tab instead of emailed, so you can still test it by checking the logs after signing up.

---

## Files changed in this update

- `pom.xml` — added `spring-boot-starter-mail`
- `entity/User.java` — added `emailVerified`, `verificationCode`, `verificationCodeExpiry`
- `service/AuthService.java`, `service/EmailService.java` (new) — verification flow
- `service/EventService.java` — venue conflict check + cascade delete
- `service/UserService.java` — cascade delete
- `service/FileStorageService.java` (new), `config/WebConfig.java` (new) — image uploads
- `controller/AuthController.java` — `/verify-email`, `/resend-code` endpoints
- `controller/EventController.java` — `/upload-image` endpoint
- `repository/EventRepository.java`, `RegistrationRepository.java`, `FeedbackRepository.java` — supporting queries
- `config/SecurityConfig.java` — fixed public browsing permissions (the bug from before)
- `config/DataSeeder.java` — seeded admin is now pre-verified
- `application.properties`, `application-prod.properties` — mail config
- Frontend: `verify-email.html` (new), `register.html`, `login.html`, `organizer-dashboard.html`, `index.html`, `events.html`, `event-details.html`, `admin-dashboard.html`, `css/style.css` (full redesign)

---

## How to apply this update to your local project

1. **Extract this zip into your existing folder** `D:\xo-event-management-2\xo-event-management` — choose "Replace files" / "Overwrite" if prompted. This only touches the files listed above; anything else you have locally (like `application-dev.properties` or your `data/` H2 folder) is untouched.
2. Fill in your SMTP credentials in `application.properties` (see above), or leave blank to use the console fallback.
3. Rebuild and test locally:
   ```
   mvn clean package -DskipTests
   java -jar target\xo-event-management.jar --spring.profiles.active=dev
   ```
4. Commit and push:
   ```
   git add .
   git commit -m "Add image upload, venue conflict check, cascade delete, email verification, new theme"
   git push
   ```
5. On Render, add the `MAIL_*` environment variables listed above, then let it redeploy (push to GitHub triggers this automatically).
