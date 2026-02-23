package com.jaeckel.mediaccc.tv.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TvNavRoutesTest {

    @Test
    fun eventDetailRouteHoldsEventGuid() {
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
    fun conferenceDetailRouteHoldsAcronym() {
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
    fun playerRouteHoldsAllProperties() {
        val route = PlayerRoute(
            eventGuid = "guid1",
            videoUrl = "https://example.com/video.mp4",
            title = "Test Talk",
            speakers = "Speaker A",
            date = "2024-01-15",
            conference = "39C3",
            duration = 3600L
        )
        assertEquals("guid1", route.eventGuid)
        assertEquals("https://example.com/video.mp4", route.videoUrl)
        assertEquals("Test Talk", route.title)
        assertEquals("Speaker A", route.speakers)
        assertEquals("2024-01-15", route.date)
        assertEquals("39C3", route.conference)
        assertEquals(3600L, route.duration)
    }

    @Test
    fun playerRouteDefaultValues() {
        val route = PlayerRoute(eventGuid = "g", videoUrl = "url")
        assertEquals("", route.title)
        assertEquals("", route.speakers)
        assertEquals("", route.date)
        assertEquals("", route.conference)
        assertEquals(0L, route.duration)
    }

    @Test
    fun playerRouteEquality() {
        val route1 = PlayerRoute(eventGuid = "g", videoUrl = "url")
        val route2 = PlayerRoute(eventGuid = "g", videoUrl = "url")
        assertEquals(route1, route2)
    }

    @Test
    fun playerRouteInequality() {
        val route1 = PlayerRoute(eventGuid = "g1", videoUrl = "url1")
        val route2 = PlayerRoute(eventGuid = "g2", videoUrl = "url2")
        assertNotEquals(route1, route2)
    }

    @Test
    fun playerRouteCopy() {
        val route = PlayerRoute(eventGuid = "g", videoUrl = "url", title = "Original")
        val copy = route.copy(title = "Updated")
        assertEquals("g", copy.eventGuid)
        assertEquals("url", copy.videoUrl)
        assertEquals("Updated", copy.title)
    }
}
