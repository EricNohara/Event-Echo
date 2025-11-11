# Event Echo - Memory Sharing App

- **Team Members:** Eric, Owen, Richard
- **Course:** CS 501 – Mobile Application Development
- **Instructor:** Ronald Czik
- **Semester:** Fall 2025

---

## Documentation

Please find our documentation (Architecture Diagram, Progress Chart, Presentation) in the /Doc directory.

---

## Table of Contents

1. [Project Overview](#-project-overview)
2. [App Flow](#-app-flow)
3. [Target Users & Problem Being Solved](#-target-users--problem-being-solved)
4. [Planned Features](#-planned-features)
   - [MVP Features](#mvp-features)
   - [Stretch Goals](#stretch-goals)
5. [APIs & Sensors](#-apis--sensors)
6. [Team Roles & Responsibilities](#-team-roles--responsibilities)
7. [Repository Info](#-repository-info)
8. [Summary](#-summary)

---

## Project Overview

**Event Echo** is a memory-sharing map app for real-world events.  
After attending an event (concert, festival, lecture, etc.), users can create an **event room** (if one doesn’t already exist) and post **one photo and one short reflection** about their experience.  
Other attendees can join the same event room and add their own “echo” — a photo and thought — building a **community-generated timeline and memory wall** for that event.

Over time, each event’s location becomes a **shared memory space**, where users can relive collective experiences and see different perspectives.

Additionally, users can **upvote or downvote** contributions, with the best ones being **pinned** to the top and earning contributor points.

---

## App Flow

1. Open the app and view a **map** with pins for nearby or upcoming events.
2. Tap a pin to view photos and reflections shared by other attendees.
3. Upload your own “echo” (one photo + one short thought) after attending.
4. Explore the **memory wall** — a grid or timeline view of user contributions.

Each location evolves into a living archive of shared memories.

---

## Target Users & Problem Being Solved

**Target Users:**

- University students (campus events, lectures, club activities)
- Music festival, conference, movie, play, or sports attendees
- Event hosts looking for community feedback and engagement

**Problems Solved:**

- After events, photos and reflections are scattered across personal galleries and social feeds.
- There’s no single place to collectively share experiences tied to a specific **time and location**.
- Event organizers lack an easy way to gather public sentiment or feedback.

Event Echo solves these by providing a **shared, location-tied memory wall**.

---

## Planned Features

### MVP Features

- **Event Map:** Interactive map showing pins for all past and upcoming events (Google Maps API).
- **Event Creation:** Users can create or join an event by selecting a location and time.
- **Memory Upload:** Each user can upload one photo and one reflection per event.
- **Memory Wall:** Scrollable timeline/grid of all uploaded memories.
- **Authentication:** Basic login/signup for user accounts.
- **Database:** Store users, events, and memories.
- **Storage:** Upload and serve user images.

### Stretch Goals

- **Event Discovery:** Filter or search events by tag, date, distance, or name (sorted by proximity and relevance).
- **Social Features:** Upvotes/downvotes, best contributor badge, event follows, notifications for nearby or new events.
- **AR Timeline:** Optional AR view showing memories in physical space.
- **Weather Integration:** Display event weather data for context.

---

## APIs & Sensors

**External APIs**

- **Google Maps API** – Interactive maps and event pinning.
- **Supabase/Firebase** – Database, file storage, and authentication.
- **Ticketmaster Discovery API** – Automatically fetch and create local event rooms.

**Device Sensors**

- **GPS** – Detect user’s location for proximity-based event features.
- **Camera** – Capture and upload event photos.

---

## Team Roles & Responsibilities

| Role                         | Responsibilities                                                          |
| ---------------------------- | ------------------------------------------------------------------------- |
| **API Integration Research** | Identify and test APIs like Google Maps, Ticketmaster, Supabase/Firebase. |
| **Backend Design**           | Define data models (events, users, memories) and system architecture.     |
| **Backend Development**      | Implement authentication, API routes, and database logic.                 |
| **Frontend Design**          | Create UI wireframes and interaction flows.                               |
| **Frontend Development**     | Build the event map, memory wall, and upload interface.                   |

_Work will be split evenly among members according to skill specialization._
