package com.jaeckel.mediaccc.ui.cast

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVKit.AVRoutePickerView
import platform.UIKit.UIColor

/**
 * iOS actual implementation of [CastButton].
 *
 * Uses [AVRoutePickerView] — the standard iOS media-routing button. When tapped it shows
 * the system AirPlay device picker so the user can send video to Apple TV or any other
 * AirPlay 2 receiver. This is the native iOS counterpart of the Android Cast button.
 *
 * ## Upgrading to GCKUICastButton
 * Once the `google-cast-sdk` CocoaPod (or GoogleCast SPM package) is added to the Xcode project
 * and the Kotlin/Native cinterop is configured, replace the [AVRoutePickerView] factory
 * body with:
 * ```kotlin
 * import platform.GoogleCast.GCKUICastButton
 * import platform.CoreGraphics.CGRectZero
 * factory = { GCKUICastButton(frame = CGRectZero.readValue()) }
 * ```
 *
 * ## Adding the CocoaPod
 * See `iosApp/Podfile` for the configuration. After running `pod install`, open
 * `iosApp.xcworkspace` (NOT `iosApp.xcodeproj`) in Xcode.
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
            AVRoutePickerView().also { picker ->
                picker.prioritizesVideoDevices = true
                picker.tintColor = UIColor.whiteColor
            }
        },
        modifier = modifier
    )
}

