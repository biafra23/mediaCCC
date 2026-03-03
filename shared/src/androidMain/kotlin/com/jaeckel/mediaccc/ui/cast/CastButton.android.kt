package com.jaeckel.mediaccc.ui.cast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener

/**
 * Android actual implementation of [CastButton].
 *
 * Renders a [MediaRouteButton] that discovers nearby Cast devices and initiates a Cast session.
 * When a session starts or resumes, [recordingUrl] is automatically loaded on the Cast device via
 * [AndroidCastManager].
 */
@Composable
actual fun CastButton(
    recordingUrl: String?,
    mimeType: String?,
    title: String?,
    modifier: Modifier
) {
    val context = LocalContext.current
    val currentTitle by rememberUpdatedState(title)
    val currentMimeType by rememberUpdatedState(mimeType)

    DisposableEffect(recordingUrl, currentMimeType) {
        val castContext = runCatching { CastContext.getSharedInstance(context) }.getOrNull()
            ?: return@DisposableEffect onDispose { }

        val manager = AndroidCastManager(castContext)

        val listener = object : SessionManagerListener<CastSession> {
            override fun onSessionStarted(session: CastSession, sessionId: String) {
                manager.loadOnSession(session, recordingUrl, currentMimeType, currentTitle)
            }

            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
                manager.loadOnSession(session, recordingUrl, currentMimeType, currentTitle)
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

        // If a session is already active when this effect runs, load media immediately —
        // the session callbacks won't fire in that case.
        val currentSession = castContext.sessionManager.currentCastSession
        if (currentSession != null) {
            manager.loadOnSession(currentSession, recordingUrl, currentMimeType, currentTitle)
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

