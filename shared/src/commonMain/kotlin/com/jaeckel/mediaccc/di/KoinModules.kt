package com.jaeckel.mediaccc.di

import com.jaeckel.mediaccc.MediaRepository
import com.jaeckel.mediaccc.StreamingRepository
import com.jaeckel.mediaccc.api.MediaCCCApi
import com.jaeckel.mediaccc.api.StreamingApi
import com.jaeckel.mediaccc.data.db.AppDatabase
import com.jaeckel.mediaccc.data.db.PlaybackHistoryDao
import com.jaeckel.mediaccc.data.repository.PlaybackHistoryRepository
import com.jaeckel.mediaccc.viewmodel.ConferenceDetailViewModel
import com.jaeckel.mediaccc.viewmodel.EventDetailViewModel
import com.jaeckel.mediaccc.viewmodel.HistoryViewModel
import com.jaeckel.mediaccc.viewmodel.HomeViewModel
import com.jaeckel.mediaccc.viewmodel.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sharedModule = module {
    single { MediaCCCApi() }
    single { StreamingApi() }
    single { MediaRepository(get<MediaCCCApi>()) }
    single { StreamingRepository(get<StreamingApi>()) }
    single<PlaybackHistoryDao> { get<AppDatabase>().playbackHistoryDao() }
    single<PlaybackHistoryRepository> { PlaybackHistoryRepository(get<PlaybackHistoryDao>()) }
    viewModel { (eventGuid: String) ->
        EventDetailViewModel(
            get<MediaRepository>(),
            get<PlaybackHistoryRepository>(),
            eventGuid
        )
    }
    viewModel { (acronym: String) -> ConferenceDetailViewModel(get<MediaRepository>(), acronym) }
    viewModel { HomeViewModel(get<MediaRepository>(), get<StreamingRepository>()) }
    viewModel { SearchViewModel(get<MediaRepository>()) }
    viewModel { HistoryViewModel(get<PlaybackHistoryRepository>()) }
}



