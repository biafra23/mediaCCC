package com.jaeckel.mediaccc.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class NavRoutesTest {

    @Test
    fun eventDetailRouteConstruction() {
        val route = EventDetailRoute(eventGuid = "abc-123")
        assertEquals("abc-123", route.eventGuid)
    }

    @Test
    fun eventDetailRouteEquality() {
        val route1 = EventDetailRoute(eventGuid = "abc")
        val route2 = EventDetailRoute(eventGuid = "abc")
        assertEquals(route1, route2)
    }

    @Test
    fun eventDetailRouteInequality() {
        val route1 = EventDetailRoute(eventGuid = "abc")
        val route2 = EventDetailRoute(eventGuid = "def")
        assertNotEquals(route1, route2)
    }

    @Test
    fun conferenceDetailRouteConstruction() {
        val route = ConferenceDetailRoute(acronym = "39c3")
        assertEquals("39c3", route.acronym)
    }

    @Test
    fun conferenceDetailRouteEquality() {
        val route1 = ConferenceDetailRoute(acronym = "39c3")
        val route2 = ConferenceDetailRoute(acronym = "39c3")
        assertEquals(route1, route2)
    }

    @Test
    fun conferenceDetailRouteInequality() {
        val route1 = ConferenceDetailRoute(acronym = "39c3")
        val route2 = ConferenceDetailRoute(acronym = "38c3")
        assertNotEquals(route1, route2)
    }

    @Test
    fun playerRouteConstruction() {
        val route = PlayerRoute(
            videoUrl = "https://example.com/video.mp4",
            title = "Test Talk",
            speakers = "Speaker A",
            date = "2024-01-15",
            conference = "39C3",
            eventGuid = "guid1"
        )
        assertEquals("https://example.com/video.mp4", route.videoUrl)
        assertEquals("Test Talk", route.title)
        assertEquals("Speaker A", route.speakers)
        assertEquals("2024-01-15", route.date)
        assertEquals("39C3", route.conference)
        assertEquals("guid1", route.eventGuid)
    }

    @Test
    fun playerRouteDefaultValues() {
        val route = PlayerRoute(videoUrl = "url")
        assertEquals("", route.title)
        assertEquals("", route.speakers)
        assertEquals("", route.date)
        assertEquals("", route.conference)
        assertNull(route.eventGuid)
    }

    @Test
    fun playerRouteEquality() {
        val route1 = PlayerRoute(videoUrl = "url")
        val route2 = PlayerRoute(videoUrl = "url")
        assertEquals(route1, route2)
    }

    @Test
    fun playerRouteInequality() {
        val route1 = PlayerRoute(videoUrl = "url1")
        val route2 = PlayerRoute(videoUrl = "url2")
        assertNotEquals(route1, route2)
    }

    @Test
    fun playerRouteCopy() {
        val route = PlayerRoute(videoUrl = "url", title = "Original")
        val copy = route.copy(title = "Updated")
        assertEquals("url", copy.videoUrl)
        assertEquals("Updated", copy.title)
    }

    @Test
    fun singletonRouteIdentity() {
        // Data objects are singletons
        assertEquals(HomeRoute, HomeRoute)
        assertEquals(SearchRoute, SearchRoute)
        assertEquals(ConferencesRoute, ConferencesRoute)
        assertEquals(FavoritesRoute, FavoritesRoute)
        assertEquals(HistoryRoute, HistoryRoute)
        assertEquals(QueueRoute, QueueRoute)
        assertEquals(SettingsRoute, SettingsRoute)
    }
}
