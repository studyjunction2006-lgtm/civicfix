# CivicFix — Community Infrastructure Issue Reporting & Resolution Platform

> **Empowering campus and civic communities to report infrastructure issues, prevent ticket duplicates, automate algorithmic prioritization, dispatch work orders, and verify transparent repairs.**

---

## 🏛️ System Architecture

CivicFix is structured as a full-stack enterprise monorepo:

```
civicfix/
├── backend/                  # Spring Boot 3 REST API + STOMP WebSocket Server
│   ├── src/main/java/com/civicfix/
│   │   ├── analytics/        # Platform KPIs, resolution rate, workload charts
│   │   ├── assignment/       # Field technician assignment & response flow
│   │   ├── audit/            # Immutable audit logging
│   │   ├── auth/             # JWT authentication, login & self-registration
│   │   ├── category/         # Issue categories & SLA deadlines
│   │   ├── common/           # Generic ApiResponse, PagedResponse, Exceptions
│   │   ├── config/           # Security, WebMvc, STOMP WebSockets, OpenApi
│   │   ├── confirmation/     # Community "+1 Confirm" duplicate prevention
│   │   ├── department/       # Campus departments (Estate, Electrical, Sanitation...)
│   │   ├── issue/            # Issue lifecycle, SLA priority calculator, geospatial search
│   │   ├── notification/     # In-app notifications & STOMP push dispatch
│   │   ├── rating/           # Citizen 1-5 star resolution satisfaction ratings
│   │   ├── security/         # JWT filter, UserPrincipal, Method Security
│   │   ├── storage/          # Photographic evidence & proof storage
│   │   └── user/             # User profiles & Admin role management
│   └── src/main/resources/
│       ├── application.yml   # Multi-profile Spring configuration
│       └── db/migration/     # Flyway PostgreSQL migrations (V1 schema, V2 seed)
│
├── frontend/                 # Modern React 18 + Vite 5 + TailwindCSS Single-Page App
│   ├── src/
│   │   ├── components/
│   │   │   ├── common/       # Navbar, Footer, DemoBar, NotificationDrawer, Modal, Badges
│   │   │   ├── map/          # Leaflet IssueMap & GPS LocationPicker
│   │   │   └── issue/        # IssueCard, TimelineStepper, ConfirmButton, Action Modals
│   │   ├── context/          # AuthContext (1-click persona switch), NotificationContext (STOMP)
│   │   ├── pages/            # LandingPage, ExplorePage, ReportIssuePage, DetailPage, Dashboards
│   │   └── services/         # Axios API client mapping all backend endpoints
│   ├── package.json
│   └── vite.config.js
│
├── database/                 # Database scripts & Flyway migrations
├── docker-compose.yml        # PostgreSQL 16 + Spring Boot containerization
├── .env.example              # Environment variables template
└── README.md
```

---

## 👥 Pre-Configured Demo Personas

CivicFix features a **Quick Demo Switcher bar** at the top of the interface, allowing one-click instant testing between all four user roles:

| Persona | Name | Role | Email | Password | Responsibilities |
|---|---|---|---|---|---|
| **Platform Dean / Admin** | Dr. Arvind Mehra | `ADMIN` | `admin@civicfix.org` | `password123` | Platform analytics, audit trail, SLA targets, user role modification |
| **Department Head** | Er. Shalini Verma | `AUTHORITY` | `authority@civicfix.org` | `password123` | Ticket triage, priority override, dispatch technician, department SLA tracking |
| **Field Technician** | Ramesh Kumar | `WORKER` | `worker@civicfix.org` | `password123` | Work orders, accept/decline, mark in-progress, upload before/after repair photos |
| **Student / Citizen** | Pulkit Sharma | `CITIZEN` | `citizen@civicfix.org` | `password123` | Report issue with GPS/photos, corroboration upvotes, verify fixes, submit 1-5 star ratings |

---

## 🚀 Key Platform Features

1. **Community Duplicate Prevention & Upvoting (+1 Confirm)**
   - Instead of 50 separate tickets for one water main leak, citizens corroborate existing reports with notes.
   - Confirmations dynamically scale the algorithmic priority score.

2. **Algorithmic SLA & Urgency Priority Calculation**
   - Automatically computes a 0–100 urgency score using severity weighting, category SLA hours, issue age, and confirmation multiplier.
   - High-hazard tickets bypass triage queues straight to emergency dispatch.

3. **Interactive Geo-Spatial Map (Leaflet / OpenStreetMap)**
   - Custom priority-colored pins with instant popup previews, photo thumbnails, and direction links.
   - Interactive GPS coordinate picker with browser geolocation detection.

4. **Transparent Audit Trail & Status Timeline**
   - Every status transition logs the actor, previous status, new status, timestamp, and justification notes immutably.

5. **Proof of Resolution & Citizen Satisfaction Verification**
   - Technicians upload photographic proof of the finished repair.
   - Reporters and confirming citizens review the fix, choose to verify or reopen, and leave 1-to-5 star quality feedback.

6. **Real-Time WebSocket Push Notifications**
   - STOMP client over SockJS subscribes to `/topic/notifications/{userId}` for instantaneous dispatch and status change alerts.

---

## 💻 Running the Monorepo Locally

### 1. Prerequisites
- **Java 21** or **17**
- **Node.js 18+** & npm
- **Docker & Docker Compose** (for PostgreSQL)

### 2. Start PostgreSQL
```bash
docker compose up -d postgres
```
*Creates `civicfix_db` on port `5432` with username `civicfix_user` and password `civicfix_secure_password_change_in_prod`.*

### 3. Start Spring Boot Backend
```bash
cd backend
mvn spring-boot:run
```
*Backend runs on `http://localhost:8080` and Swagger UI is accessible at `http://localhost:8080/swagger-ui.html`.*

### 4. Start React Frontend
```bash
cd frontend
npm install
npm run dev
```
*Frontend runs on `http://localhost:5173`.*

---

## 📡 REST API Reference

| Controller | Method | Endpoint | Description |
|---|---|---|---|
| **Auth** | `POST` | `/api/v1/auth/login` | Authenticate and obtain JWT token |
| | `POST` | `/api/v1/auth/register` | Register new citizen account |
| | `GET` | `/api/v1/auth/me` | Fetch authenticated user profile |
| **Issues** | `GET` | `/api/v1/issues` | Search, filter, and paginate issues |
| | `POST` | `/api/v1/issues` | Submit new issue report with GPS & photos |
| | `GET` | `/api/v1/issues/{id}` | Get full issue details and audit trail |
| | `GET` | `/api/v1/issues/tracking/{trackingNumber}` | Look up issue by tracking number (e.g. `CF-2026-0001`) |
| | `PATCH` | `/api/v1/issues/{id}/status` | Advance status with audit reason & proof |
| | `PATCH` | `/api/v1/issues/{id}/priority` | Authority priority override |
| | `GET` | `/api/v1/issues/public/map` | Public geo-coordinates for Leaflet map |
| **Confirmations**| `POST` | `/api/v1/confirmations/issues/{issueId}` | Citizen "+1 Confirm" issue |
| | `GET` | `/api/v1/confirmations/issues/{issueId}` | List confirmations for issue |
| **Assignments** | `POST` | `/api/v1/assignments/issues/{issueId}` | Dispatch worker to issue |
| | `POST` | `/api/v1/assignments/{id}/respond` | Worker accept or decline |
| | `GET` | `/api/v1/assignments/my` | Get current worker's tasks |
| **Ratings** | `POST` | `/api/v1/ratings/issues/{issueId}` | Submit 1-5 star resolution review |
| **Notifications**| `GET` | `/api/v1/notifications` | Paginated notifications |
| | `PATCH` | `/api/v1/notifications/{id}/read` | Mark notification as read |
| **Analytics** | `GET` | `/api/v1/analytics/public` | Public landing page metrics |
| | `GET` | `/api/v1/analytics/summary` | Dashboard KPI summary & breakdown |
| **Storage** | `POST` | `/api/v1/storage/upload` | Upload photographic proof |
| **Admin** | `GET` | `/api/v1/admin/users` | Manage users and roles |
| | `GET` | `/api/v1/admin/audit-logs` | Inspect platform audit history |
