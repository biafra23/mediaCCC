package com.jaeckel.mediaccc.ui.cast

/**
 * Platform-agnostic interface for controlling a Cast (Chromecast / AirPlay) session.
 *
 * Implement this interface per platform:
 * - **Android**: delegate to Google Cast SDK ([com.google.android.gms.cast.framework]).
 * - **iOS**: delegate to the Google Cast iOS SDK (GoogleCast framework, added via CocoaPods or SPM).
 */
interface CastManager {
    /**
     * Load and play [url] on the currently active Cast session.
     *
     * @param url      Direct URL of the media stream to cast.
     * @param mimeType MIME type of the stream (e.g. "video/mp4"). `null` falls back to "video/mp4".
     * @param title    Optional human-readable title sent as media metadata.
     */
    fun loadMedia(url: String, mimeType: String?, title: String?)
}
