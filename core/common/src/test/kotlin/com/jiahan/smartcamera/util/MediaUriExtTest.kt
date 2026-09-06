package com.jiahan.smartcamera.util

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jiahan.smartcamera.domain.MediaUri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Pins the conversion at the domain boundary.
 *
 * [MediaUri] exists so repository contracts carry no `android.net.Uri`, which makes these two
 * functions the only place a platform URI is allowed to cross -- and a lossy conversion here is
 * invisible until a note renders the wrong media. What matters is therefore not that each function
 * runs but that the pair round-trips: whatever the picker handed down comes back byte-identical.
 *
 * Robolectric rather than a plain JVM test, and that is the point rather than an inconvenience: a
 * stubbed `Uri` would make every assertion below pass without exercising any parsing. The cases are
 * chosen to be the ones a naive `String` wrapper would get wrong -- percent-encoding, a query
 * string, a fragment, spaces.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class MediaUriExtTest {

    private fun assertRoundTrips(raw: String) {
        val original = raw.toUri()

        val restored = original.toMediaUri().toPlatformUri()

        assertEquals(original, restored)
        assertEquals(raw, restored.toString())
    }

    @Test
    fun `toMediaUri carries the string form of the uri`() {
        val uri = "content://media/external/images/media/42".toUri()

        assertEquals(MediaUri("content://media/external/images/media/42"), uri.toMediaUri())
    }

    @Test
    fun `toPlatformUri resolves back to an equal uri`() {
        val mediaUri = MediaUri("content://media/external/images/media/42")

        assertEquals(Uri.parse("content://media/external/images/media/42"), mediaUri.toPlatformUri())
    }

    @Test
    fun `content uri round-trips`() {
        assertRoundTrips("content://media/external/images/media/42")
    }

    @Test
    fun `file uri round-trips`() {
        assertRoundTrips("file:///data/user/0/com.jiahan.smartcamera/cache/photo.jpg")
    }

    /** The camera hands back a `FileProvider` authority rather than a MediaStore one. */
    @Test
    fun `file provider uri round-trips`() {
        assertRoundTrips("content://com.jiahan.smartcamera.fileprovider/cache/IMG_20260906.jpg")
    }

    @Test
    fun `percent-encoded path round-trips`() {
        assertRoundTrips("content://media/external/images/media/My%20Photo%20%281%29.jpg")
    }

    @Test
    fun `unencoded spaces round-trip`() {
        assertRoundTrips("file:///storage/emulated/0/DCIM/Camera Roll/photo 1.jpg")
    }

    @Test
    fun `query string and fragment round-trip`() {
        assertRoundTrips("https://example.com/photo.jpg?size=large&id=1#top")
    }

    /**
     * `Uri.toString()` preserves case in the path while normalising nothing else, so a value that
     * differs only by case must stay distinct -- media ids are case-sensitive.
     */
    @Test
    fun `case is preserved`() {
        val lower = MediaUri("content://media/external/images/media/abc")
        val upper = MediaUri("content://media/external/images/media/ABC")

        assertEquals("content://media/external/images/media/abc", lower.toPlatformUri().toString())
        assertEquals("content://media/external/images/media/ABC", upper.toPlatformUri().toString())
    }
}