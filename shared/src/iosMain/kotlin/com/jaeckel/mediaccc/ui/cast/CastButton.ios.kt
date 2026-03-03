package com.jaeckel.mediaccc.ui.cast

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import mediaccc.shared.generated.resources.Res
import mediaccc.shared.generated.resources.cast_button
import org.jetbrains.compose.resources.stringResource
import platform.AVKit.AVRoutePickerView
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIApplication
import platform.UIKit.UIButton
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

private const val ROUTE_PICKER_SIZE = 44.0
private const val ROUTE_PICKER_TOP_MARGIN = 40.0

/**
 * iOS actual implementation of [CastButton].
 *
 * Shows a Material [Icons.Filled.Cast] icon button. When tapped, an [AVRoutePickerView] is added
 * to the key window and its internal button is triggered, which presents the system AirPlay device
 * picker (Apple TV, AirPlay 2 receivers). This approach is used instead of embedding
 * [AVRoutePickerView] directly in a Compose [UIKitView] because the system picker presentation
 * requires the view to have a proper UIViewController in its responder chain, which CMP's UIKit
 * interop layer cannot always guarantee.
 *
 * ## Upgrading to GCKUICastButton
 * Once the `google-cast-sdk` CocoaPod (or GoogleCast SPM package) is added to the Xcode project
 * and the Kotlin/Native cinterop is configured, you can replace this with a `UIKitView` wrapping
 * `GCKUICastButton`:
 * ```kotlin
 * import platform.GoogleCast.GCKUICastButton
 * import platform.CoreGraphics.CGRectZero
 * UIKitView(factory = { GCKUICastButton(frame = CGRectZero.readValue()) }, modifier = modifier)
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
    // Keep a single AVRoutePickerView instance for the lifetime of this composable.
    // It is added to the key window (invisible) so that it has a proper UIViewController
    // in its responder chain when the system picker is shown.
    val routePickerView = remember {
        AVRoutePickerView().also { picker ->
            picker.prioritizesVideoDevices = true
            picker.hidden = true  // invisible in the window; hidden doesn't block programmatic action dispatch
        }
    }

    DisposableEffect(routePickerView) {
        onDispose { routePickerView.removeFromSuperview() }
    }

    IconButton(
        onClick = { showRoutePicker(routePickerView) },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Cast,
            contentDescription = stringResource(Res.string.cast_button),
            tint = Color.White
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun showRoutePicker(routePickerView: AVRoutePickerView) {
    val keyWindow: UIWindow = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { it.windows }
        .firstOrNull { it.isKeyWindow }
        ?: run {
            println("CastButton: key window not found — AirPlay picker cannot be presented")
            return
        }

    // Add to the key window (not yet added, or was removed after a previous dismissal)
    if (routePickerView.superview == null) {
        keyWindow.addSubview(routePickerView)
        // Position at top-right edge — hidden, but within window bounds so the responder chain includes the root VC
        val w = keyWindow.bounds.useContents { size.width }
        routePickerView.setFrame(CGRectMake(w - ROUTE_PICKER_SIZE, ROUTE_PICKER_TOP_MARGIN, ROUTE_PICKER_SIZE, ROUTE_PICKER_SIZE))
    }

    // AVRoutePickerView's sole subview is its internal UIButton. Triggering it causes the
    // system to present the AirPlay route picker from the view's nearest UIViewController.
    val internalButton = routePickerView.subviews.firstOrNull() as? UIButton
    if (internalButton == null) {
        println("CastButton: AVRoutePickerView internal button not found — AirPlay picker cannot be shown")
        return
    }
    internalButton.sendActionsForControlEvents(UIControlEventTouchUpInside)
}

