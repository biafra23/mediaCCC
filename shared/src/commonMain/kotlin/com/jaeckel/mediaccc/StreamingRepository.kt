package com.jaeckel.mediaccc

import com.jaeckel.mediaccc.api.StreamingApi
import com.jaeckel.mediaccc.api.model.streaming.StreamingConference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StreamingRepository(private val api: StreamingApi) {

    fun getStreams(): Flow<Result<List<StreamingConference>>> = flow {
        try {
            val result = api.getStreams()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
