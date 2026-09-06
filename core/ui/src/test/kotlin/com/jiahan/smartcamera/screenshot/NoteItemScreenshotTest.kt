package com.jiahan.smartcamera.screenshot

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.jiahan.smartcamera.common.NoteItem
import com.jiahan.smartcamera.domain.DetectedLabel
import com.jiahan.smartcamera.domain.MediaDetail
import com.jiahan.smartcamera.domain.Note
import com.jiahan.smartcamera.ui.theme.SmartPhotosTheme
import org.junit.Test
import kotlin.time.Instant

/**
 * Roborazzi screenshot tests for [NoteItem]. Each test captures a PNG that is diffed against a
 * checked-in reference under `src/test/screenshots`, catching visual/layout regressions that
 * semantic (text) assertions cannot.
 *
 * Deterministic by construction: no remote image URLs (the account-circle fallback is drawn
 * instead of a network image), so Coil never performs I/O during rendering.
 *
 * Every case is captured in both themes. Dark is not a formality here -- it is the half where a
 * hardcoded colour or a token read from the wrong scheme actually shows, and the light capture
 * cannot see it. The callbacks are passed by name rather than as eight positional `{}`s: they are
 * all no-ops, so reordering [NoteItem]'s parameters would rebind them silently, with neither a
 * compile error nor a golden diff to notice it.
 */
class NoteItemScreenshotTest : BaseScreenshotTest() {

    /** Captures [note] in [darkTheme], with every callback a named no-op. */
    private fun captureNote(note: Note, darkTheme: Boolean) {
        capture {
            SmartPhotosTheme(darkTheme = darkTheme) {
                // The background the app actually paints: SmartPhotosApp wraps the nav host in
                // `Surface(color = colorScheme.background)` and no feature screen paints its own.
                // Without it a capture lands on the host's default light ground -- which flatters
                // a light golden and makes a dark one plainly wrong: dark app bar, light body.
                Surface(color = MaterialTheme.colorScheme.background) {
                    NoteItem(
                        note = note,
                        onNavigateToNotePreview = {},
                        onEditNote = {},
                        onToggleFavorite = {},
                        onDeleteNote = {},
                        onPhotoClick = {},
                        onVideoClick = {},
                        onProfilePictureClick = {},
                        onShareNote = {},
                    )
                }
            }
        }
    }

    @Test
    fun noteItem_textOnly_light() = captureNote(sampleNote, darkTheme = false)

    @Test
    fun noteItem_textOnly_dark() = captureNote(sampleNote, darkTheme = true)

    @Test
    fun noteItem_favorited_light() = captureNote(favoritedNote, darkTheme = false)

    @Test
    fun noteItem_favorited_dark() = captureNote(favoritedNote, darkTheme = true)

    @Test
    fun noteItem_withMediaThumbnail_light() = captureNote(noteWithMedia, darkTheme = false)

    @Test
    fun noteItem_withMediaThumbnail_dark() = captureNote(noteWithMedia, darkTheme = true)

    @Test
    fun noteItem_withVideoThumbnail_light() = captureNote(noteWithVideo, darkTheme = false)

    @Test
    fun noteItem_withVideoThumbnail_dark() = captureNote(noteWithVideo, darkTheme = true)

    @Test
    fun noteItem_longContent_light() = captureNote(noteWithLongContent, darkTheme = false)

    @Test
    fun noteItem_longContent_dark() = captureNote(noteWithLongContent, darkTheme = true)

    private companion object {
        val sampleNote = Note(
            noteId = "note1",
            username = "john_doe",
            text = "Hello, this is a preview note with some sample text that wraps across multiple lines.",
            mediaList = null,
            profilePictureUrl = null,
            isFavorite = false,
            createdDate = Instant.fromEpochMilliseconds(1_700_000_000_000L),
        )

        val favoritedNote = sampleNote.copy(
            noteId = "note2",
            username = "jane_doe",
            text = "This note is marked as a favourite.",
            isFavorite = true,
        )

        val noteWithMedia = sampleNote.copy(
            noteId = "note3",
            text = "A note with an attached media thumbnail.",
            mediaList = listOf(
                MediaDetail(
                    photoUrl = "",
                    generatedTexts = listOf("a cat on a sofa"),
                    generatedLabels = listOf(DetectedLabel("Cat", 0.98)),
                )
            ),
        )

        /**
         * A video attachment, which draws the play-arrow badge that
         * [com.jiahan.smartcamera.common.MediaThumbnail] overlays on its `isVideo` branch. The
         * photo fixture above never reaches it, so a regression in that badge -- its size, its
         * translucent circle, or the scheme colours it reads -- moved no golden before this one
         * existed.
         */
        val noteWithVideo = sampleNote.copy(
            noteId = "note4",
            text = "A note with an attached video.",
            mediaList = listOf(
                MediaDetail(
                    videoUrl = "",
                    thumbnailUrl = "",
                    isVideo = true,
                )
            ),
        )

        /**
         * Both of the item's truncation rules at once: the username is capped at one line and the
         * body at fifteen, each with an ellipsis. The other fixtures are short enough that
         * `maxLines` and `TextOverflow.Ellipsis` could be dropped from either `Text` without
         * changing a single pixel of their goldens.
         */
        val noteWithLongContent = sampleNote.copy(
            noteId = "note5",
            username = "a_username_far_too_long_to_fit_beside_the_timestamp",
            text = (1..20).joinToString(" ") { "Line $it of a note that runs well past the cap." },
        )
    }
}