# 🌿 GreenThumb — Houseplant Management API

A houseplant management platform Backend **Spring Boot 4**, **PostgreSQL**, **JWT authentication**

---

## Description

GreenThumb helps plant owners track their houseplant collections, set recurring care schedules (watering,
fertilizing, repotting), log care activity, and receive enriched care tips sourced from the **Perenual Plant API**.
Admins manage a global species catalogue and monitor platform-wide activity.

---

## Tech Stack

| Layer       | Technology                                     |
|-------------|------------------------------------------------|
| Backend     | Java 17, Spring Boot 4, Spring Security, JPA |
| Auth        | JWT (jjwt 0.13.0), BCrypt, Email verification  |
| Database    | PostgreSQL (prod), H2 (dev)                    |
| File Upload | Cloudinary                                     |
| Email       | Spring Mail + Thymeleaf HTML templates         |
| Third-party | Perenual Plant API (care tips + toxicity)      |
| Testing     | JUnit 5, Mockito                               |
| DevOps      | Docker, Docker Compose, Spring Profiles        |

---

## General Approach

The backend is structured using a **feature-based package layout** (`feature/auth`, `feature/plant`, `feature/species`,
etc.) following the interface/implementation service pattern. Every endpoint is individually unit-tested using Mockito.
JWT authentication is stateless with role-based access control (`USER` /
`ADMIN`) enforced via `@PreAuthorize`.

The Perenual API integration follows a **cache-first pattern** — results are stored in a `species_tips_cache` table and
served from cache for 7 days before re-fetching, preventing unnecessary external calls.

---

## API Endpoints

| Method | URL                                | Description               | Access  |
|--------|------------------------------------|---------------------------|---------|
| POST   | /auth/register                     | Register new account      | Public  |
| GET    | /auth/verify?token=                | Verify email              | Public  |
| POST   | /auth/login                        | Login → JWT               | Public  |
| POST   | /auth/forgot-password              | Request reset link        | Public  |
| POST   | /auth/reset-password               | Reset with token          | Public  |
| GET    | /api/species                       | Browse species catalogue  | Public  |
| GET    | /api/species/{id}                  | Get species details       | Public  |
| GET    | /api/species/{id}/reviews          | Get species reviews       | Public  |
| GET    | /api/species/{id}/tips             | Get care tips (Perenual)  | Private |
| GET    | /api/my-plants                     | List my plants            | Private |
| POST   | /api/my-plants                     | Add plant to collection   | Private |
| GET    | /api/my-plants/due-today           | Plants needing care today | Private |
| GET    | /api/my-plants/{id}                | Get plant details         | Private |
| PUT    | /api/my-plants/{id}                | Update plant              | Private |
| DELETE | /api/my-plants/{id}                | Remove plant              | Private |
| POST   | /api/my-plants/{id}/photo          | Upload plant photo        | Private |
| GET    | /api/my-plants/{id}/schedule       | Get care schedules        | Private |
| POST   | /api/my-plants/{id}/schedule       | Create care schedule      | Private |
| PUT    | /api/my-plants/{id}/schedule/{sid} | Update schedule           | Private |
| DELETE | /api/my-plants/{id}/schedule/{sid} | Deactivate schedule       | Private |
| POST   | /api/my-plants/{id}/care-log       | Log a care action         | Private |
| GET    | /api/my-plants/{id}/care-log       | Get care history          | Private |
| POST   | /api/my-plants/{id}/reviews        | Write a review            | Private |
| PUT    | /api/reviews/{id}                  | Update review             | Private |
| DELETE | /api/reviews/{id}                  | Delete review             | Private |
| GET    | /api/users/profile                 | Get my profile            | Private |
| PUT    | /api/users/profile                 | Update my profile         | Private |
| POST   | /api/users/profile/picture         | Upload profile picture    | Private |
| POST   | /api/users/change-password         | Change password           | Private |
| GET    | /api/admin/users                   | List all users            | Admin   |
| DELETE | /api/admin/users/{id}              | Soft-delete user          | Admin   |
| POST   | /api/species                       | Create species            | Admin   |
| PUT    | /api/species/{id}                  | Update species            | Admin   |
| DELETE | /api/species/{id}                  | Delete species            | Admin   |
| POST   | /api/species/{id}/image            | Upload species image      | Admin   |
| GET    | /api/admin/stats                   | Platform statistics       | Admin   |

---

## Docker (Prod)

Use the production profile with PostgreSQL via Docker Compose.

```bash
# Copy environment variables
cp .env.example .env

# Start the API + database (prod profile)
docker compose up --build
```

---

## ERD

> [ERD](https://app.eraser.io/workspace/sYmXWTWngfI5OEs0UvGb?origin=share&diagram=C0mqwseHibVfc9D2t11lS)

---


## Installation

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 15+ (for prod)

### Running the App

```bash
# Clone the repo
git clone https://github.com/Hashedx99/GreenThumb.git

# Copy and fill environment variables
cp ../.env.example .env

# Run in dev mode (H2 in-memory, no DB setup needed)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Seed Data

On first startup (dev profile), the app seeds:

- 1 admin: `admin@greenthumb.com` / `Admin@1234`
- 3 users: `hamza@greenthumb.com`, `jasim@greenthumb.com`, `ali@greenthumb.com` / `User@1234`
- 6 plant species
- Sample plants with care schedules (including 2 due-today entries for demo)

---

## Unsolved Problems / Hurdles

- Perenual's free tier returns limited species results for some scientific names — the service falls back gracefully to
  default tips rather than failing the request.
