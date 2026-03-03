package com.jaeckel.mediaccc.mobile.cast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener

/**
 * Composable that renders a Google Cast (Chromecast) button in the app bar.
 *
 * The button uses [MediaRouteButton] to show nearby Cast devices and initiates a Cast session.
 * When a session starts or resumes, the provided [recordingUrl] is automatically loaded on the
 * Cast device for playback.
 *
 * @param recordingUrl URL of the media to cast. When non-null, it is loaded as soon as a Cast
 *   session becomes active.
 * @param mimeType MIME type of the media (e.g. "video/mp4", "video/webm"). When null, falls back
 *   to "video/mp4".
 * @param title Optional display title sent as Cast media metadata.
 * @param modifier Modifier applied to the underlying [AndroidView].
 */
@Composable
fun CastButton(
    recordingUrl: String?,
    mimeType: String? = null,
    title: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTitle by rememberUpdatedState(title)
    val currentMimeType by rememberUpdatedState(mimeType)

    DisposableEffect(recordingUrl, currentMimeType) {
        val castContext = runCatching { CastContext.getSharedInstance(context) }.getOrNull()
            ?: return@DisposableEffect onDispose { }

        val listener = object : SessionManagerListener<CastSession> {
            override fun onSessionStarted(session: CastSession, sessionId: String) {
                loadMediaOnCast(session, recordingUrl, currentMimeType, currentTitle)
            }

            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
                loadMediaOnCast(session, recordingUrl, currentMimeType, currentTitle)
            }

            override fun onSessionEnded(session: CastSession, error: Int) {}
            override fun onSessionStarting(session: CastSession) {}
            override fun onSessionStartFailed(session: CastSession, error: Int) {}
            override fun onSessionEnding(session: CastSession) {}
            override fun onSessionResuming(session: CastSession, sessionId: String) {}
            override fun onSessionResumeFailed(session: CastSession, error: Int) {}
            override fun onSessionSuspended(session: CastSession, reason: Int) {}
        }

        castContext.sessionManager.addSessionManagerListener(listener, CastSession::class.java)

        // If a session is already active when this effect runs (e.g. the composable entered
        // composition after a session was established, or recordingUrl just became non-null),
        // load the media immediately — the session callbacks won't fire in that case.
        val currentSession = castContext.sessionManager.currentCastSession
        if (currentSession != null) {
            loadMediaOnCast(currentSession, recordingUrl, currentMimeType, currentTitle)
        }

        onDispose {
            castContext.sessionManager.removeSessionManagerListener(listener, CastSession::class.java)
        }
    }

    AndroidView(
        factory = { ctx ->
            MediaRouteButton(ctx).also { button ->
                CastButtonFactory.setUpMediaRouteButton(ctx, button)
            }
        },
        modifier = modifier
    )
}

private fun loadMediaOnCast(session: CastSession, url: String?, mimeType: String?, title: String?) {
    if (url == null) return
    val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
        if (!title.isNullOrBlank()) putString(MediaMetadata.KEY_TITLE, title)
    }
    val mediaInfo = MediaInfo.Builder(url)
        .setContentType(mimeType ?: "video/mp4")
        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
        .setMetadata(metadata)
        .build()
    val loadOptions = MediaLoadOptions.Builder()
        .setAutoplay(true)
        .build()
    session.remoteMediaClient?.load(mediaInfo, loadOptions)
}
