package com.jaeckel.mediaccc.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun SystemAppearance(isFullscreen: Boolean) {
    val context = LocalContext.current
    
    // Log to both Logcat and stdout to be sure
    SideEffect {
        Log.d("SystemAppearance", "Composing SystemAppearance with isFullscreen=$isFullscreen")
        println("SystemAppearance: Composing SystemAppearance with isFullscreen=$isFullscreen")
    }

    DisposableEffect(isFullscreen) {
        val activity = context.findActivity() as? ComponentActivity
        
        if (activity == null) {
            Log.e("SystemAppearance", "Activity not found! Context: $context")
            println("SystemAppearance: Activity not found! Context: $context")
            return@DisposableEffect onDispose { }
        }
        
        if (isFullscreen) {
            Log.d("SystemAppearance", "Applying fullscreen system bar styles")
            println("SystemAppearance: Applying fullscreen system bar styles")
            
            // 1. Configure Edge-to-Edge to use BLACK for system bars
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK),
                navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK)
            )

            // 2. Hide system bars for immersive experience
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())

            // 3. Handle display cutout (notch)
            val originalCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode
            } else {
                0
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val params = window.attributes
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = params
            }

            onDispose {
                Log.d("SystemAppearance", "Restoring default system bar styles")
                println("SystemAppearance: Restoring default system bar styles")
                
                // Show bars again
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT

                // Restore transparent/auto style
                activity.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                )

                // Restore cutout mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val params = window.attributes
                    params.layoutInDisplayCutoutMode = originalCutoutMode
                    window.attributes = params
                }
            }
        } else {
            onDispose { }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
