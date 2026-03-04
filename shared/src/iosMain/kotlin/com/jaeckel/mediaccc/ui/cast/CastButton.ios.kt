package com.jaeckel.mediaccc.ui.cast

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIColor
// Use the cocoapods.GoogleCast package as configured in build.gradle.kts
import cocoapods.google_cast_sdk.GCKUICastButton

/**
 * iOS actual implementation of [CastButton].
 *
 * Uses [GCKUICastButton] from the Google Cast iOS SDK.
 * The button is configured to be black for better visibility.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CastButton(
    recordingUrl: String?,
    mimeType: String?,
    title: String?,
    modifier: Modifier
) {
    UIKitView(
        factory = {
            val button = GCKUICastButton(frame = CGRectZero.readValue())
            button.tintColor = UIColor.blackColor
            button
        },
        modifier = modifier,
        update = { castButton ->
             castButton.tintColor = UIColor.blackColor
        }
    )
}
