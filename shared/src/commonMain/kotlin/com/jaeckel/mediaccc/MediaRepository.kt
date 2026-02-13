package com.jaeckel.mediaccc

import com.jaeckel.mediaccc.api.MediaCCCApi
import com.jaeckel.mediaccc.api.model.Conference
import com.jaeckel.mediaccc.api.model.ConferencesResponse
import com.jaeckel.mediaccc.api.model.Event
import com.jaeckel.mediaccc.api.model.EventsResponse
import com.jaeckel.mediaccc.api.model.Recording
import com.jaeckel.mediaccc.api.model.RecordingsResponse
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

    fun getConference(acronym: String): Flow<Result<Conference>> = flow {
        try {
            val result = api.getConference(acronym)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getEvents(): Flow<Result<EventsResponse>> = flow {
        try {
            val result = api.getEvents()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getEvent(guid: String): Flow<Result<Event>> = flow {
        try {
            val result = api.getEvent(guid)
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

    fun getUnPopularEvents(year: Int): Flow<Result<EventsResponse>> = flow {
        try {
            val result = api.getUnPopularEvents(year)
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

    fun getPromotedEvents(): Flow<Result<EventsResponse>> = flow {
        try {
            val result = api.getPromotedEvents()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getRecordings(): Flow<Result<RecordingsResponse>> = flow {
        try {
            val result = api.getRecordings()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getRecording(id: String): Flow<Result<Recording>> = flow {
        try {
            val result = api.getRecording(id)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
