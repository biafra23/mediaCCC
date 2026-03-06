package com.jaeckel.mediaccc.ui.cast

import cocoapods.google_cast_sdk.GCKCastContext
import cocoapods.google_cast_sdk.GCKCastSession
import cocoapods.google_cast_sdk.GCKMediaInformationBuilder
import cocoapods.google_cast_sdk.GCKMediaLoadOptions
import cocoapods.google_cast_sdk.GCKMediaMetadata
import cocoapods.google_cast_sdk.GCKMediaMetadataTypeMovie
import cocoapods.google_cast_sdk.GCKMediaStreamTypeBuffered
import cocoapods.google_cast_sdk.kGCKMetadataKeyTitle
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL

/**
 * iOS implementation of [CastManager].
 *
 * Loads media on the currently active Cast session via [GCKCastSession.remoteMediaClient].
 * Mirrors the logic of AndroidCastManager.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IOSCastManager : CastManager {

    override fun loadMedia(url: String, mimeType: String?, title: String?) {
        val session = GCKCastContext.sharedInstance().sessionManager.currentCastSession ?: return
        loadOnSession(session, url, mimeType, title)
    }

    /** Load media on an already-established [session]. */
    fun loadOnSession(session: GCKCastSession, url: String?, mimeType: String?, title: String?) {
        val mediaUrl = url ?: return
        val client = session.remoteMediaClient ?: return

        val metadata = GCKMediaMetadata(metadataType = GCKMediaMetadataTypeMovie)
        if (!title.isNullOrBlank()) {
            metadata.setString(title, forKey = kGCKMetadataKeyTitle)
        }

        val contentUrl = NSURL.URLWithString(mediaUrl) ?: return
        val builder = GCKMediaInformationBuilder(contentURL = contentUrl)
        builder.streamType = GCKMediaStreamTypeBuffered
        builder.contentType = mimeType ?: "video/mp4"
        builder.metadata = metadata
        val mediaInfo = builder.build()

        val loadOptions = GCKMediaLoadOptions()
        loadOptions.autoplay = true
        client.loadMedia(mediaInfo, withOptions = loadOptions)
    }
}
