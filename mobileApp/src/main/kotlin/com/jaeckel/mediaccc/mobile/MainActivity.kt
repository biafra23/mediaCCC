package com.jaeckel.mediaccc.mobile

import android.app.PictureInPictureParams
import android.os.Bundle
import android.util.Rational
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import com.jaeckel.mediaccc.mobile.cast.CastButton
import com.jaeckel.mediaccc.ui.navigation.AppNavHost
import com.jaeckel.mediaccc.ui.util.PipState

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AppNavHost(
                    eventDetailExtraActions = { recordingUrl ->
                        CastButton(recordingUrl = recordingUrl)
                    }
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (PipState.isVideoPlaying.value) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }
}

