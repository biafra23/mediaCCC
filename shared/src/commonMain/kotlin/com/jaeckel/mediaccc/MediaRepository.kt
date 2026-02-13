package com.jaeckel.mediaccc

import com.jaeckel.mediaccc.api.MediaCCCApi
import com.jaeckel.mediaccc.api.model.ConferencesResponse
import com.jaeckel.mediaccc.api.model.EventsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MediaRepository(private val api: MediaCCCApi) {

    fun getConferences(): Flow<Result<ConferencesResponse>> = flow {
        try {
            val result = api.getConferences()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun searchEvents(query: String): Flow<Result<EventsResponse>> = flow {
        try {
            val result = api.search(query)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getPopularEvents(year: Int): Flow<Result<EventsResponse>> = flow {
        try {
            val result = api.getPopularEvents(year)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getRecentEvents(): Flow<Result<EventsResponse>> = flow {
        try {
            val result = api.getRecentEvents()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}


