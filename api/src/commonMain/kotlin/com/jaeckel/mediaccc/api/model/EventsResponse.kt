package com.jaeckel.mediaccc.api.model

import kotlinx.serialization.Serializable

@Serializable
data class EventsResponse(
    val events: List<Event>
)

