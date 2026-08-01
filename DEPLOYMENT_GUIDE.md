# Deploying XO Event Management to the Cloud (Live 24/7 URL)

This guide gets your app running on the internet permanently, with a real URL, without needing your PC turned on. We use **Render** as the primary path (genuinely free to start, no credit card) with **Railway** as an alternative if you prefer MySQL over PostgreSQL.

> **What changed in this project for cloud deployment:**
> - Added the PostgreSQL driver to `pom.xml` (alongside MySQL, so local dev is untouched)
> - Added `application-prod.properties` — a cloud config that reads everything from environment variables (no secrets hardcoded)
> - Added `Dockerfile` and `render.yaml` so Render can build and wire everything automatically

---

## Part 1 — Push your project to GitHub

Render (and Railway) deploy by connecting to a Git repository, so your code needs to be on GitHub first.

1. Go to https://github.com and create a free account if you don't have one.
2. Click **New repository** → name it `xo-event-management` → keep it Public or Private (either works) → **Create repository**.
3. On your PC, open Command Prompt in your project folder:
   ```
   cd D:\xo-event-management-2\xo-event-management
   git init
   git add .
   git commit -m "Initial commit - XO Event Management"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/xo-event-management.git
   git push -u origin main
   ```
   (If `git` isn't recognized, install it from https://git-scm.com/download/win first.)

---

## Part 2 — Deploy on Render (recommended path)

### Step 1 — Create a Render account
Go to https://render.com and sign up (GitHub login is easiest — it also lets Render see your repos). No credit card required for the free tier.

### Step 2 — Deploy using the Blueprint (one-click, automatic)
This project already includes a `render.yaml` file that tells Render exactly what to create.

1. In the Render dashboard, click **New +** → **Blueprint**.
2. Connect your GitHub account if prompted, then select your `xo-event-management` repository.
3. Render will detect `render.yaml` and show you a preview: one **Web Service** (`xo-event-management`) and one **PostgreSQL database** (`xo-event-db`), both on the free plan.
4. Click **Apply**. Render will:
   - Provision the free PostgreSQL database
   - Build your Docker image (using the included `Dockerfile`)
   - Wire up `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, and a random `JWT_SECRET` automatically
   - Start the web service

This first build takes about 5–10 minutes (Maven download + Docker build). You can watch progress live in the **Logs** tab.

### Step 3 — Get your live URL
Once deployed, Render shows a URL like:
```
https://xo-event-management.onrender.com
```
Open it — that's your app, live on the internet, reachable from any device, anywhere.

### Step 4 — Log in
Use the same seeded admin account:
- Email: `admin@xoevents.com`
- Password: `Admin@123`

(The `DataSeeder` runs automatically on first startup against the new PostgreSQL database, just like it did locally with MySQL/H2.)

### Important free-tier behavior to expect
- The web service **spins down after 15 minutes of no traffic** and takes ~30–60 seconds to wake up on the next request. This is normal for Render's free tier — not a bug.
- The free PostgreSQL database **expires 30 days after creation** unless you upgrade it to a paid plan (~$6/month) before then. You'll get an email warning beforehand. For a personal project or portfolio piece this is usually fine — just note the date.

### If you'd rather set it up manually (without the Blueprint)
1. **New +** → **PostgreSQL** → name it `xo-event-db` → Free plan → Create. Note the **Host**, **Port**, **Database**, **Username**, **Password** shown on its info page.
2. **New +** → **Web Service** → connect your repo → Environment: **Docker** → Free plan.
3. Under **Environment Variables**, add:
   | Key | Value |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DB_HOST` | *(from the database info page)* |
   | `DB_PORT` | *(from the database info page)* |
   | `DB_NAME` | `xo_event_db` |
   | `DB_USER` | *(from the database info page)* |
   | `DB_PASSWORD` | *(from the database info page)* |
   | `JWT_SECRET` | *(any long random string, e.g. generate one at* https://randomkeygen.com *)* |
4. Click **Create Web Service**.

---

## Part 3 — Alternative: Deploy on Railway (if you want to keep MySQL)

Railway supports MySQL natively, so you wouldn't need the PostgreSQL switch — but note Railway's free trial is now just a one-time $5 credit for 30 days, after which you need the $5/month Hobby plan to keep it running.

1. Go to https://railway.com and sign up.
2. **New Project** → **Deploy from GitHub repo** → select `xo-event-management`.
3. **+ New** → **Database** → **Add MySQL**. Railway provisions it and shows connection variables (`MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`).
4. On your web service, go to **Variables** and add:
   | Key | Value |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `railway` |
   | `SPRING_DATASOURCE_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}` |
   | `SPRING_DATASOURCE_USERNAME` | `${{MySQL.MYSQLUSER}}` |
   | `SPRING_DATASOURCE_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
   | `JWT_SECRET` | *(a long random string)* |
5. Add a small `application-railway.properties` (mirrors `application-prod.properties` but with `spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver` and `hibernate.dialect=org.hibernate.dialect.MySQLDialect`) if you go this route — happy to generate that file for you if you pick Railway.
6. Railway auto-detects the port via the `PORT` environment variable, same as Render.
7. Once deployed, Railway gives you a public URL under **Settings → Domains** (click **Generate Domain** for a free `*.up.railway.app` URL).

---

## Part 4 — Optional: a custom domain name

Both Render and Railway let you attach your own domain (e.g. `xoevents.com`) instead of the default `*.onrender.com` / `*.up.railway.app` subdomain:
1. Buy a domain from any registrar (Namecheap, GoDaddy, Google Domains, etc.) — typically $10–15/year.
2. In your Render/Railway service settings, find **Custom Domains**, add your domain.
3. Copy the CNAME/A record they give you into your domain registrar's DNS settings.
4. Wait for DNS to propagate (a few minutes to a few hours) — then your app is live at your own domain, with free SSL handled automatically.

---

## Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| Build fails on Render with a Maven error | Check the **Logs** tab — usually a missing dependency; make sure you pushed the updated `pom.xml` |
| App builds but crashes on start with a datasource error | Double-check the `DB_HOST/PORT/NAME/USER/PASSWORD` env vars match exactly what's on your database's info page |
| "Application Error" / 502 page | The service is likely still spinning up (free tier cold start) — wait ~60 seconds and refresh |
| Login fails with a 500 error | `JWT_SECRET` env var might be missing — set it manually if the Blueprint didn't generate one |
| Site loads but events/categories are empty | This is expected on a fresh database — the `DataSeeder` only adds the admin account and categories; create events via the Organizer dashboard |

---

Once deployed, this app is reachable from **any phone, tablet, or computer, anywhere with internet** — no VPN, no same-WiFi requirement, no need to keep your own PC running.
