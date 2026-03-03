package com.jaeckel.mediaccc.ui.cast

/**
 * iOS stub implementation of [CastManager].
 *
 * This class is a placeholder until the Google Cast iOS SDK (GoogleCast CocoaPod / SPM package)
 * is integrated. Once integrated, replace this with a real implementation that uses
 * `GCKSessionManager` to load media on active Cast sessions.
 *
 * Example full implementation (requires `google-cast-sdk` pod):
 * ```kotlin
 * import platform.GoogleCast.GCKCastContext
 *
 * internal class IOSCastManager : CastManager {
 *     override fun loadMedia(url: String, mimeType: String?, title: String?) {
 *         val session = GCKCastContext.sharedInstance().sessionManager.currentCastSession
 *             ?: return
 *         // Build GCKMediaInformation and load via session.remoteMediaClient
 *     }
 * }
 * ```
 */
internal class IOSCastManager : CastManager {
    override fun loadMedia(url: String, mimeType: String?, title: String?) {
        // TODO: Integrate Google Cast iOS SDK (add 'google-cast-sdk' CocoaPod or SPM package)
        //       and implement GCKSessionManager-based media loading here.
        println("IOSCastManager: loadMedia called for '$url' — Cast SDK not yet integrated on iOS.")
    }
}
