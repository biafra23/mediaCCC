package com.jaeckel.mediaccc.ui.cast

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific Cast (Chromecast) button rendered in the event-detail top app bar.
 *
 * - **Android**: wraps [androidx.mediarouter.app.MediaRouteButton] via the Google Cast SDK.
 * - **iOS**: wraps a native UIKit button (upgradeable to [GCKUICastButton] once the
 *   GoogleCast CocoaPod / SPM package is added to the Xcode project).
 *
 * @param recordingUrl URL of the media to cast. Passed to [CastManager.loadMedia] when a session
 *   becomes active.
 * @param mimeType     MIME type of the media (e.g. "video/mp4"). `null` falls back to "video/mp4".
 * @param title        Optional display title sent as Cast media metadata.
 * @param modifier     Modifier applied to the button.
 */
@Composable
expect fun CastButton(
    recordingUrl: String?,
    mimeType: String? = null,
    title: String? = null,
    modifier: Modifier = Modifier
)
