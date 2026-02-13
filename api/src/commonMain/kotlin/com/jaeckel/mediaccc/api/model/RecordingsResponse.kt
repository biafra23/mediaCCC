package com.jaeckel.mediaccc.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RecordingsResponse(
    val recordings: List<Recording>
)

