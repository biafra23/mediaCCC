package com.jaeckel.mediaccc.api

import com.jaeckel.mediaccc.api.model.streaming.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StreamingModelsTest {

    @Test
    fun `StreamingConference default values`() {
        val conf = StreamingConference(conference = "Test", slug = "test")
        assertEquals("Test", conf.conference)
        assertEquals("test", conf.slug)
        assertNull(conf.author)
        assertNull(conf.description)
        assertNull(conf.keywords)
        assertNull(conf.schedule)
        assertNull(conf.startsAt)
        assertNull(conf.endsAt)
        assertFalse(conf.isCurrentlyStreaming)
        assertTrue(conf.groups.isEmpty())
    }

    @Test
    fun `StreamingConference with streaming flag`() {
        val conf = StreamingConference(conference = "Live", slug = "live", isCurrentlyStreaming = true)
        assertTrue(conf.isCurrentlyStreaming)
    }

    @Test
    fun `StreamGroup holds rooms`() {
        val room = StreamRoom(slug = "room1", display = "Room 1")
        val group = StreamGroup(group = "Lectures", rooms = listOf(room))
        assertEquals("Lectures", group.group)
        assertEquals(1, group.rooms.size)
        assertEquals("Room 1", group.rooms[0].display)
    }

    @Test
    fun `StreamRoom default values`() {
        val room = StreamRoom(slug = "room1", display = "Room 1")
        assertNull(room.guid)
        assertNull(room.schedulename)
        assertNull(room.thumb)
        assertNull(room.poster)
        assertNull(room.link)
        assertNull(room.stream)
        assertNull(room.talks)
        assertTrue(room.streams.isEmpty())
    }

    @Test
    fun `RoomTalks current and next`() {
        val current = Talk(title = "Current Talk", speaker = "Speaker A")
        val next = Talk(title = "Next Talk", speaker = "Speaker B")
        val talks = RoomTalks(current = current, next = next)
        assertEquals("Current Talk", talks.current?.title)
        assertEquals("Speaker A", talks.current?.speaker)
        assertEquals("Next Talk", talks.next?.title)
    }

    @Test
    fun `RoomTalks with null talks`() {
        val talks = RoomTalks(current = null, next = null)
        assertNull(talks.current)
        assertNull(talks.next)
    }

    @Test
    fun `Talk default values`() {
        val talk = Talk()
        assertNull(talk.title)
        assertNull(talk.speaker)
        assertNull(talk.guid)
        assertNull(talk.code)
        assertNull(talk.track)
        assertNull(talk.special)
        assertNull(talk.fstart)
        assertNull(talk.fend)
        assertNull(talk.start)
        assertNull(talk.end)
        assertNull(talk.duration)
    }

    @Test
    fun `Talk with all fields`() {
        val talk = Talk(
            fstart = "10:00",
            fend = "11:00",
            start = 1000L,
            end = 2000L,
            duration = 1000L,
            guid = "talk-guid",
            code = "ABC",
            track = "Security",
            title = "Security Talk",
            speaker = "Expert",
            roomKnown = true,
            optout = false,
            url = "https://example.com/talk"
        )
        assertEquals("10:00", talk.fstart)
        assertEquals("11:00", talk.fend)
        assertEquals(1000L, talk.start)
        assertEquals(1000L, talk.duration)
        assertEquals("Security", talk.track)
        assertEquals(true, talk.roomKnown)
        assertEquals(false, talk.optout)
    }

    @Test
    fun `RoomStream properties`() {
        val url = StreamUrl(display = "HLS", tech = "HLS", url = "https://example.com/stream.m3u8")
        val stream = RoomStream(
            slug = "hd-native",
            display = "HD Native",
            type = "video",
            urls = mapOf("hls" to url)
        )
        assertEquals("video", stream.type)
        assertEquals("hd-native", stream.slug)
        assertFalse(stream.isTranslated)
        assertNull(stream.videoSize)
        assertEquals(1, stream.urls.size)
        assertEquals("https://example.com/stream.m3u8", stream.urls["hls"]?.url)
    }

    @Test
    fun `RoomStream translated`() {
        val stream = RoomStream(
            slug = "translated",
            display = "Translated",
            type = "audio",
            isTranslated = true,
            videoSize = listOf(1920, 1080)
        )
        assertTrue(stream.isTranslated)
        assertEquals(listOf(1920, 1080), stream.videoSize)
    }

    @Test
    fun `StreamUrl construction`() {
        val url = StreamUrl(url = "https://example.com/stream")
        assertNull(url.display)
        assertNull(url.tech)
        assertEquals("https://example.com/stream", url.url)
    }

    @Test
    fun `StreamUrl with all fields`() {
        val url = StreamUrl(display = "WebM", tech = "VP9", url = "https://example.com/stream.webm")
        assertEquals("WebM", url.display)
        assertEquals("VP9", url.tech)
    }

    @Test
    fun `full streaming hierarchy`() {
        val streamUrl = StreamUrl(url = "https://example.com/stream.m3u8")
        val roomStream = RoomStream(slug = "hd", display = "HD", type = "video", urls = mapOf("hls" to streamUrl))
        val talk = Talk(title = "Talk", speaker = "Speaker")
        val room = StreamRoom(
            slug = "saal1",
            display = "Saal 1",
            talks = RoomTalks(current = talk, next = null),
            streams = listOf(roomStream)
        )
        val group = StreamGroup(group = "Lectures", rooms = listOf(room))
        val conference = StreamingConference(
            conference = "38C3",
            slug = "38c3",
            isCurrentlyStreaming = true,
            groups = listOf(group)
        )

        assertEquals("38C3", conference.conference)
        assertTrue(conference.isCurrentlyStreaming)
        assertEquals("Saal 1", conference.groups[0].rooms[0].display)
        assertEquals("Talk", conference.groups[0].rooms[0].talks?.current?.title)
        assertEquals("https://example.com/stream.m3u8",
            conference.groups[0].rooms[0].streams[0].urls["hls"]?.url)
    }
}
