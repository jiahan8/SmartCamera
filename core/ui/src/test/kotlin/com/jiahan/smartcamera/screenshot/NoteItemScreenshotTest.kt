package com.jiahan.smartcamera.screenshot

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
 */
class NoteItemScreenshotTest : BaseScreenshotTest() {

    @Test
    fun noteItem_textOnly_light() {
        capture {
            SmartPhotosTheme(darkTheme = false) {
                NoteItem(
                    sampleNote,
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {})
            }
        }
    }

    @Test
    fun noteItem_textOnly_dark() {
        capture {
            SmartPhotosTheme(darkTheme = true) {
                NoteItem(
                    sampleNote,
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {})
            }
        }
    }

    @Test
    fun noteItem_favorited_light() {
        capture {
            SmartPhotosTheme(darkTheme = false) {
                NoteItem(
                    favoritedNote,
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {})
            }
        }
    }

    @Test
    fun noteItem_withMediaThumbnail_light() {
        capture {
            SmartPhotosTheme(darkTheme = false) {
                NoteItem(
                    noteWithMedia,
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {})
            }
        }
    }

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
    }
}