# Aura

**A native Android music player engineered around a modular, local-first media architecture.**

Aura is an Android music application built with **Kotlin and Jetpack Compose**.

Rather than concentrating playback, media scanning, database access, UI, and feature state inside one application module, Aura is structured as a multi-module product with dedicated core, data, domain, feature, and service layers.

> **Status:** active development.

## Product direction

Aura is being built as a refined personal music experience with an emphasis on:

- responsive native playback,
- local media discovery,
- clean library organization,
- albums and artists,
- playlists,
- queue control,
- lyrics,
- search,
- listening statistics,
- a dedicated player experience,
- maintainable Android architecture.

## Architecture

Aura is intentionally modular.

```text
app
 │
 ├── core
 │    ├── common
 │    ├── database
 │    ├── datastore
 │    ├── designsystem
 │    ├── model
 │    ├── media
 │    ├── network
 │    ├── permissions
 │    ├── playback
 │    ├── scanner
 │    ├── analytics
 │    └── utilities
 │
 ├── data
 │    └── repository
 │
 ├── domain
 │    └── playback
 │
 ├── feature
 │    ├── home
 │    ├── library
 │    ├── songs
 │    ├── albums
 │    ├── artists
 │    ├── playlists
 │    ├── search
 │    ├── player
 │    ├── queue
 │    ├── lyrics
 │    ├── flow
 │    ├── statistics
 │    └── settings
 │
 └── service
      └── playback
```

This keeps feature UI separate from lower-level playback, persistence, media scanning, and shared infrastructure.

## Key engineering areas

### Native playback

Aura uses AndroidX **Media3** components for playback/session infrastructure.

### Media scanning

A dedicated scanner module separates device-media discovery from presentation logic.

### Playback service

Playback is isolated in its own service module so audio state can outlive individual Compose screens.

### Persistence

Room provides structured local persistence.

DataStore is available for lightweight user/application preferences.

### Dependency injection

Hilt provides dependency injection across modules.

### Repository layer

Data access is abstracted into repository modules instead of allowing feature screens to depend directly on persistence or media sources.

### Compose UI

Jetpack Compose and Material 3 provide the declarative UI layer.

### Images

Coil is used for image loading within the Compose application.

### Concurrency

Kotlin Coroutines and Flow support asynchronous work and reactive state propagation.

## Technology stack

### Language / UI

- Kotlin
- Jetpack Compose
- Material 3

### Architecture

- Multi-module Android architecture
- Hilt
- ViewModel
- Coroutines / Flow

### Playback

- AndroidX Media3
- ExoPlayer
- Media Session

### Local data

- Room
- DataStore

### Networking

- Retrofit
- OkHttp
- Kotlin serialization / Moshi support

### Images

- Coil

### Tooling

- Gradle Kotlin DSL
- Version Catalog
- KSP
- Timber
- LeakCanary


## Requirements

The project currently targets a modern Android toolchain.

Recommended:

- Android Studio with current AGP support
- JDK 17+
- Android SDK installed

## Build

Clone:

```bash
git clone https://github.com/sebitsamir/aura.git
cd aura
```

On Windows:

```bat
gradlew.bat assembleDebug
```

On macOS/Linux:

```bash
./gradlew assembleDebug
```

Install/run from Android Studio for normal development.

## Why the modular structure matters

A music player has unusually long-lived state.

Playback may continue while:

- the user navigates between screens,
- the app moves to the background,
- the device library changes,
- metadata is refreshed,
- queue state changes.

Keeping playback, media scanning, persistence, and feature UI separated makes those flows easier to reason about and test than a single-module implementation.

## Current status

Aura is **under active development**.

The repository already contains a substantial modular architecture, but the project should not yet be presented as a finished Play Store product unless a release build has actually been published.

## README migration note

Earlier versions of this repository were based on a reusable Android starter and the old README still described the repository as **“Android Template.”**

Aura is now the product. This README replaces that obsolete project identity.

## Author

**Sebit Samir**

GitHub: [@sebitsamir](https://github.com/sebitsamir)
