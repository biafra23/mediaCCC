# Chaos TV

A Kotlin Multiplatform app for browsing and watching talks from [media.ccc.de](https://media.ccc.de) and live streams from [streaming.media.ccc.de](https://streaming.media.ccc.de). Built with Compose Multiplatform, targeting Android phones, Android TV, and iOS.

## Features

### Browse & Discover
- **Home** — Featured events, recent talks, and a "Continue Watching" row for resuming playback
- **Live Streams** — When conferences are streaming, a "Live Now" row appears at the top of the home screen with room cards and current talk info
- **Conferences** — Browse all conferences sorted by most recent, with tag-based filtering
- **Conference Detail** — View all events for a conference, filterable by tags
- **Search** — Full-text search across events with tag chips for quick filtering

### Video Playback
- **Inline player** on the event detail screen with fullscreen toggle
- **Live stream player** with play/pause, seek bar, and a LIVE indicator (red when at the live edge, gray when seeked back — tap to jump back to live)
- **Resume playback** — progress is saved automatically and restored when returning to a video
- **Continue Watching** — videos in progress appear on the home screen (hidden once ≥97.5% complete)
- **Language selector** — choose the recording language when multiple are available
- **Picture-in-Picture** on Android — video continues in a floating window when leaving the app

### TV Experience
- Dedicated Android TV interface with D-pad navigation and focus management
- Hero carousel for promoted events
- Custom player overlay with D-pad-controlled seek bar (left/right ±10s, center = pause/play, up = dismiss)
- Live stream row on TV home screen

### Localization
- English, German, and Spanish
- Per-app language selection on Android 13+ (via system settings)

### Error Handling
- Retry buttons on all error states
- Graceful handling of network failures

## Project Structure

```
mediaCCC/
├── api/              # KMP module — Ktor HTTP clients for media.ccc.de and streaming APIs
├── shared/           # KMP module — Compose UI, navigation, ViewModels, Room DB, DI
├── mobileApp/        # Android phone app
├── tvApp/            # Android TV app
└── iosApp/           # iOS app (Xcode project)
```

### Module Overview

| Module | Description |
|--------|-------------|
| **api** | `MediaCCCApi` (conferences, events, recordings) and `StreamingApi` (live streams from streaming.media.ccc.de) with Ktor + kotlinx.serialization |
| **shared** | All Compose screens, navigation (Navigation 3), ViewModels, Room database for playback history, Koin DI, and the `StreamingRepository` / `MediaRepository` |
| **mobileApp** | Android entry point — `MainActivity` with PiP support, HLS dependency, locale config |
| **tvApp** | Android TV entry point — `TvActivity` with custom `AndroidTVPlayer`, TV Material 3 components |
| **iosApp** | iOS entry point consuming the shared KMP framework |

## Tech Stack

| Component | Library | Version |
|-----------|---------|---------|
| Language | Kotlin Multiplatform | 2.3.10 |
| UI | Compose Multiplatform | 1.10.0 |
| Material Design | Material 3 | 1.10.0-alpha05 |
| Networking | Ktor Client | 3.4.0 |
| Database | Room (KMP) | 2.7.0 |
| DI | Koin | 4.1.1 |
| Image Loading | Coil 3 | 3.3.0 |
| Video (shared) | ComposeMediaPlayer | 0.8.7 |
| Video (tvApp) | Compose Multiplatform Media Player | 1.0.53 |
| Navigation | Navigation 3 | 1.0.0-alpha05 |
| Date/Time | kotlinx-datetime | 0.7.1 |

**Android targets:** minSdk 28, compileSdk 36

## Build & Run

### Prerequisites
- Android Studio (latest stable)
- JDK 11+
- Xcode 15+ (for iOS)

### Android (Phone)
```shell
./gradlew :mobileApp:assembleDebug
```

### Android TV
```shell
./gradlew :tvApp:assembleDebug
```

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run on a simulator or device.

## APIs

The app consumes two public APIs:

- **media.ccc.de API** — `https://api.media.ccc.de/public/` — conferences, events, recordings
- **Streaming API** — `https://streaming.media.ccc.de/streams/v2.json` — live stream metadata (rooms, HLS URLs, current/next talks)

## License

This project is not affiliated with the Chaos Computer Club (CCC). Content is provided by [media.ccc.de](https://media.ccc.de).
