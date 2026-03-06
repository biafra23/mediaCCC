package com.jaeckel.mediaccc.ui.cast

import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession

/**
 * Android implementation of [CastManager].
 *
 * Loads media on the currently active Cast session via [CastSession.remoteMediaClient].
 */
internal class AndroidCastManager(private val castContext: CastContext) : CastManager {

    override fun loadMedia(url: String, mimeType: String?, title: String?) {
        val session = castContext.sessionManager.currentCastSession ?: return
        loadOnSession(session, url, mimeType, title)
    }

    /** Load media on an already-established [session]. */
    fun loadOnSession(session: CastSession, url: String?, mimeType: String?, title: String?) {
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
}
