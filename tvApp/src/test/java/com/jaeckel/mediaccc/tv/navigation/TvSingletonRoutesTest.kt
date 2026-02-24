package com.jaeckel.mediaccc.tv.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class TvSingletonRoutesTest {

    @Test
    fun homeRouteSingletonEquality() {
        assertEquals(HomeRoute, HomeRoute)
    }

    @Test
    fun searchRouteSingletonEquality() {
        assertEquals(SearchRoute, SearchRoute)
    }

    @Test
    fun conferencesRouteSingletonEquality() {
        assertEquals(ConferencesRoute, ConferencesRoute)
    }

    @Test
    fun favoritesRouteSingletonEquality() {
        assertEquals(FavoritesRoute, FavoritesRoute)
    }

    @Test
    fun historyRouteSingletonEquality() {
        assertEquals(HistoryRoute, HistoryRoute)
    }

    @Test
    fun settingsRouteSingletonEquality() {
        assertEquals(SettingsRoute, SettingsRoute)
    }
}
