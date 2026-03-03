package com.jaeckel.mediaccc.ui.cast

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIImage

/**
 * iOS actual implementation of [CastButton].
 *
 * Renders a native UIKit button using the "airplayvideo" SF Symbol as a visual placeholder for the
 * Cast action. This keeps the UI consistent while the full Google Cast iOS SDK integration is
 * pending.
 *
 * Cast session management is handled by [IOSCastManager], which currently logs a TODO until the
 * real SDK is integrated.
 *
 * ## Upgrading to GCKUICastButton
 * Once the `google-cast-sdk` CocoaPod (or GoogleCast SPM package) is added to the Xcode project
 * and the Kotlin/Native cinterop is configured:
 * 1. Update [IOSCastManager.loadMedia] to use `GCKSessionManager`.
 * 2. Replace the `UIKitView` factory body here with:
 *    ```kotlin
 *    import platform.GoogleCast.GCKUICastButton
 *    import platform.CoreGraphics.CGRectZero
 *    factory = { GCKUICastButton(frame = CGRectZero.readValue()) }
 *    ```
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
            UIButton.buttonWithType(UIButtonTypeSystem).also { button ->
                val icon = UIImage.systemImageNamed("airplayvideo")
                button.setImage(icon, forState = UIControlStateNormal)
            }
        },
        modifier = modifier
    )
}

