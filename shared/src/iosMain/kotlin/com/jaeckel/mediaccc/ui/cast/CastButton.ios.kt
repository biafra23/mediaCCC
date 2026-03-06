package com.jaeckel.mediaccc.ui.cast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import cocoapods.google_cast_sdk.GCKCastContext
import cocoapods.google_cast_sdk.GCKCastSession
import cocoapods.google_cast_sdk.GCKSessionManager
import cocoapods.google_cast_sdk.GCKSessionManagerListenerProtocol
import cocoapods.google_cast_sdk.GCKUICastButton
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIColor
import platform.darwin.NSObject

/**
 * iOS actual implementation of [CastButton].
 *
 * Wraps a native [GCKUICastButton] from the Google Cast iOS SDK and registers a
 * [GCKSessionManagerListenerProtocol] so that media is loaded on the Cast session as soon as it
 * starts — matching the Android SessionManagerListener pattern.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CastButton(
    recordingUrl: String?,
    mimeType: String?,
    title: String?,
    modifier: Modifier
) {
    // rememberUpdatedState ensures the listener closure always sees the latest values
    // without needing to re-register the listener on every recomposition.
    val currentUrl by rememberUpdatedState(recordingUrl)
    val currentMimeType by rememberUpdatedState(mimeType)
    val currentTitle by rememberUpdatedState(title)

    DisposableEffect(Unit) {
        val castManager = IOSCastManager()
        val sessionManager = GCKCastContext.sharedInstance().sessionManager

        val listener = object : NSObject(), GCKSessionManagerListenerProtocol {
            // Called when the user selects a Cast device and a new session is established.
            override fun sessionManager(
                sessionManager: GCKSessionManager,
                didStartCastSession: GCKCastSession
            ) {
                castManager.loadOnSession(didStartCastSession, currentUrl, currentMimeType, currentTitle)
            }

            // Called when a previously suspended session (e.g. app foregrounded) is resumed.
            override fun sessionManager(
                sessionManager: GCKSessionManager,
                didResumeCastSession: GCKCastSession
            ) {
                castManager.loadOnSession(didResumeCastSession, currentUrl, currentMimeType, currentTitle)
            }
        }

        sessionManager.addListener(listener)

        // If a Cast session is already active when this composable first appears
        // (e.g. navigating to a new event while a session is open), load immediately.
        sessionManager.currentCastSession?.let { session ->
            castManager.loadOnSession(session, currentUrl, currentMimeType, currentTitle)
        }

        onDispose {
            sessionManager.removeListener(listener)
        }
    }

    UIKitView(
        factory = {
            GCKUICastButton(frame = CGRectZero.readValue()).apply {
                tintColor = UIColor.blackColor
            }
        },
        modifier = modifier,
        update = { castButton ->
            castButton.tintColor = UIColor.blackColor
        }
    )
}
