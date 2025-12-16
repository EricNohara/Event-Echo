# Event Echo - Memory Sharing App

- **Team Members:** Eric, Owen, Richard
- **Course:** CS 501 – Mobile Application Development
- **Instructor:** Ronald Czik
- **Semester:** Fall 2025

---

## Documentation

Please find our documentation (Final Report with AI Reflection, Presentation Slides, Application Screenshots, Architecture Diagram, Progress Chart, ...) in the /doc directory.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Build/Run Instructions](#buildrun-instructions)
3. [App Flow](#app-flow)
4. [Target Users & Problem Being Solved](#target-users--problem-being-solved)
5. [Current Features](#current-features)
6. [Planned Features](#planned-features)
   - [Stretch Goals](#stretch-goals-descoped)
7. [APIs & Sensors](#apis--sensors)
8. [Feature List](#feature-list)
9. [Testing Strategy](#testing-strategy)
10. [Team Roles & Responsibilities](#team-roles--responsibilities)


---

## Project Overview

**Event Echo** is a memory-sharing map app for real-world events.  
After attending an event (concert, festival, lecture, etc.), users can create an **event room** (if one doesn’t already exist) and post **one photo and one short reflection** about their experience.  
Other attendees can join the same event room and add their own “echo” — a photo and thought — building a **community-generated timeline and memory wall** for that event.

Over time, each event’s location becomes a **shared memory space**, where users can relive collective experiences and see different perspectives.

Additionally, users can **upvote or downvote** contributions, with the best ones being **pinned** to the top and earning contributor points.

---

## Build/Run Instructions

1. **Download/Clone** this repository.
2. Open the project folder in **Android Studio** (download Android Studio if not installed).
3. **Sync** the project using **gradle** (Either automatically on startup or after).
4. Obtain **API keys** for **Google Maps**, **Ticketmaster,** and **Places**, and set them in the project's **local.properties** as **TICKETMASTER_API_KEY**, **MAPS_API_KEY**, and **PLACES_API_KEY**.
5. **Run the app** in Android Studio using a built-in emulator or connected mobile device.

---

## App Flow

1. Open the app and view a filterable **events** and/or browse **map** with pins for nearby or upcoming events.
2. Tap an event or pin to view photos and reflections shared by other attendees.
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

## Current Features

- **Event Map:** Interactive map showing pins for all past and upcoming events (Google Maps API).
- **Event Grid:** Functional collection of events using Ticketmaster API (ViewModel + StateFlow), formatted in a clickable grid.
- **Event Discovery:** Filter or search events by date, distance, source, or name (sorted by proximity and relevance).
- **Event Detail:** Users can view individual events in more detail.
- **Event Creation:** Users can create or join an event by selecting a location, time, and/or other filters.
- **Memory Upload:** Each user can upload one photo and one reflection per event.
- **Memory Wall:** Scrollable timeline/grid of all uploaded memories, with integrated upvotes.
- **Authentication:** Basic login/signup for user accounts.
- **Profile:** Users can create and edit authenticated profiles from which they can see their attended, created, and recent events.
- **Firebase Database:** Store users, events, and memories.
- **Navigation:** Users can navigate using top and bottom bars, profile menu, clickable components, and back buttons (Jetpack Navigation).

---

## Planned Features

### Stretch Goals (Descoped)

- **Many Social Features:** Best contributor badge, event follows, notifications for nearby or new events.
- **AR Timeline:** Optional AR view showing memories in physical space.
- **Weather Integration:** Display event weather data for context.

---

## APIs & Sensors

**External APIs**

- **Google Maps API** – Interactive maps and event pinning.
- **Firebase** – Database, file storage, and authentication.
- **Ticketmaster Discovery API** – Automatically fetch and create local event rooms.
- **Places API** - Autocomplete location-based searches.

**Device Sensors**

- **GPS** – Detect user’s location for proximity-based event features.
- **Camera** – Capture and upload event photos.

---

## Feature List
- Google Maps Display
- Firebase & Authentication
- Sign In / Sign Up
- API Integration
- GPS, Camera Sensor Integration
- Event Map
- Event Creation
- Light/Dark Mode Persistence
- Automated Testing, Logging
- High-Quality UI
- Memory Wall and Memory Upload

## Testing Strategy
- Utilize Logs & Logcat to write and view log/error messages
- Use test data to quickly test UI changes
- Test through the UI as an end user would
- Thorough testing for edge cases and unintended behavior by messing with UI
- Automated Compose UI Testing
- View data in Firebase to ensure correct values

---

## Team Roles & Responsibilities

| Role                         | Responsibilities                                                          |
| ---------------------------- | ------------------------------------------------------------------------- |
| **API Integration Research** | Identify and test APIs like Google Maps, Ticketmaster, Supabase/Firebase. |
| **Backend Design**           | Define data models (events, users, memories) and system architecture.     |
| **Backend Development**      | Implement authentication, API routes, and database logic.                 |
| **Frontend Design**          | Create UI wireframes and interaction flows.                               |
| **Frontend Development**     | Build the event map, memory wall, and upload interface.                   |

_Work split evenly among members according to skill specialization and interest._
