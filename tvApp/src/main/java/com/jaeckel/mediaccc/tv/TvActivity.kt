package com.jaeckel.mediaccc.tv

import android.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jaeckel.mediaccc.Greeting

class TvActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvEntryScreen()
        }
    }
}

@Composable
fun TvEntryScreen() {
    MaterialTheme {
        val greeting = remember { Greeting().greet() }
        Text("Compose: $greeting",  color = androidx.compose.ui.graphics.Color.White)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    TvEntryScreen()
}
