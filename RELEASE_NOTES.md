# Chaos TV — v0.5.0-alpha

## What's New

### Chromecast Support (PR #1)
- Cast talks to Chromecast-enabled devices from EventDetailScreen
- Automatic media loading when Cast session is already active
- Crash fixes for MediaRouteButton, translucent backgrounds, and CastContext initialization

### Settings Screen (PR #3, #4)
- Implemented Settings screen with **Clear Watch History** functionality
- Version string now displayed on all platforms (Android, iOS)

### Tablet Landscape Layout (PR #6)
- YouTube-style tablet landscape layout for EventDetailScreen
- Icon-only navigation with split-column design
- Fixed nested scroll issues

### Bug Fixes
- Fixed video URL mixup between live streams and queue playback (PR #5)
- iOS build fixes
- Shared debug keystore for consistent nightly builds

**Full Changelog**: https://github.com/biafra23/mediaCCC/compare/v0.4.0-alpha...v0.5.0-alpha
