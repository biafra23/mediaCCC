package com.jaeckel.mediaccc.tv.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data class EventDetailRoute(val eventGuid: String) : NavKey

@Serializable
data class PlayerRoute(
    val videoUrl: String,
    val title: String = "",
    val speakers: String = "",
    val date: String = "",
    val conference: String = ""
) : NavKey


