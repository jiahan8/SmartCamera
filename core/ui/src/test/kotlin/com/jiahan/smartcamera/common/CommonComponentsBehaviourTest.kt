package com.jiahan.smartcamera.common

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jiahan.smartcamera.core.ui.R
import com.jiahan.smartcamera.ui.theme.SmartPhotosTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Behaviour of the shared `common/` components — the half a golden cannot see.
 *
 * A screenshot proves a component *looks* right; it says nothing about whether its callbacks are
 * wired to the right thing. `PasswordField` is the clearest case: hoisting `visible` out means the
 * component only reports the toggle, and a golden of the masked state is identical whether
 * `onVisibilityChange` fires with the right value or never fires at all.
 *
 * Same Robolectric configuration as [ScrollToTopEffectTest], and deliberately not extending
 * `BaseScreenshotTest` for the same reason: these assertions are about interaction, not pixels.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    application = Application::class,
    sdk = [35],
    qualifiers = RobolectricDeviceQualifiers.Pixel5
)
class CommonComponentsBehaviourTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(resId: Int) =
        ApplicationProvider.getApplicationContext<Application>().getString(resId)

    // -------------------------------------------------------------------------
    // PasswordField
    // -------------------------------------------------------------------------

    /**
     * The rendered text, not the node's text generally: a password field exposes both `InputText`
     * (the raw value, for autofill and IME) and `EditableText` (what is actually drawn). `onNodeWithText`
     * matches either, so it finds "hunter2000" in both states and can say nothing about masking --
     * only `EditableText` distinguishes them.
     */
    private fun renderedPasswordText(): String =
        composeRule.onNodeWithText("Password")
            .fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
            .text

    @Test
    fun passwordField_masksTheValueUntilVisibilityIsRequested() {
        composeRule.setContent {
            SmartPhotosTheme {
                PasswordField(
                    value = "hunter2000",
                    onValueChange = {},
                    label = "Password",
                    visible = false,
                    onVisibilityChange = {},
                )
            }
        }

        assertEquals("\u2022".repeat("hunter2000".length), renderedPasswordText())
    }

    @Test
    fun passwordField_showsTheValueWhenVisible() {
        composeRule.setContent {
            SmartPhotosTheme {
                PasswordField(
                    value = "hunter2000",
                    onValueChange = {},
                    label = "Password",
                    visible = true,
                    onVisibilityChange = {},
                )
            }
        }

        assertEquals("hunter2000", renderedPasswordText())
    }

    /** The toggle reports the *requested* state, not the current one. */
    @Test
    fun passwordField_toggleRequestsTheOppositeVisibility() {
        var requested: Boolean? = null
        composeRule.setContent {
            SmartPhotosTheme {
                PasswordField(
                    value = "pw",
                    onValueChange = {},
                    label = "Password",
                    visible = false,
                    onVisibilityChange = { requested = it },
                )
            }
        }

        composeRule.onNodeWithContentDescription(string(R.string.cd_show_password)).performClick()

        assertEquals(true, requested)
    }

    @Test
    fun passwordField_reportsEveryEdit() {
        var latest: String? = null
        composeRule.setContent {
            SmartPhotosTheme {
                PasswordField(
                    value = "",
                    onValueChange = { latest = it },
                    label = "Password",
                    visible = true,
                    onVisibilityChange = {},
                )
            }
        }

        composeRule.onNodeWithText("Password").performTextInput("abc")

        assertEquals("abc", latest)
    }

    // -------------------------------------------------------------------------
    // SearchBar
    // -------------------------------------------------------------------------

    @Test
    fun searchBar_rendersThePlaceholderWhileEmpty_andReportsTyping() {
        var latest: String? = null
        composeRule.setContent {
            SmartPhotosTheme {
                SearchBar(searchQuery = "", onSearchQueryChange = { latest = it }) {
                    Text("Search notes")
                }
            }
        }

        composeRule.onNodeWithText("Search notes").assertIsDisplayed()

        composeRule.onNodeWithText("Search notes").performTextInput("cat")

        assertEquals("cat", latest)
    }

    // -------------------------------------------------------------------------
    // DeleteNoteConfirmationDialog
    //
    // Both buttons sit in the same dialog and both dismiss it on screen, so nothing visual
    // separates a confirm from a cancel -- only which callback fired does.
    // -------------------------------------------------------------------------

    @Test
    fun deleteDialog_confirmInvokesDeleteOnly() {
        var confirmed = false
        var dismissed = false
        composeRule.setContent {
            SmartPhotosTheme {
                DeleteNoteConfirmationDialog(
                    onDismissRequest = { dismissed = true },
                    onConfirmDelete = { confirmed = true },
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.delete)).performClick()

        assertTrue(confirmed)
        assertEquals(false, dismissed)
    }

    @Test
    fun deleteDialog_cancelInvokesDismissOnly() {
        var confirmed = false
        var dismissed = false
        composeRule.setContent {
            SmartPhotosTheme {
                DeleteNoteConfirmationDialog(
                    onDismissRequest = { dismissed = true },
                    onConfirmDelete = { confirmed = true },
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.cancel)).performClick()

        assertTrue(dismissed)
        assertEquals(false, confirmed)
    }

    // -------------------------------------------------------------------------
    // BottomSheetActionItem
    // -------------------------------------------------------------------------

    @Test
    fun bottomSheetActionItem_rendersItsLabelAndReportsClicks() {
        var clicks = 0
        composeRule.setContent {
            SmartPhotosTheme {
                BottomSheetActionItem(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = { clicks++ },
                )
            }
        }

        composeRule.onNodeWithText("Share").assertIsDisplayed()
        composeRule.onNodeWithText("Share").performClick()

        assertEquals(1, clicks)
    }

    // -------------------------------------------------------------------------
    // ProfileAvatar
    // -------------------------------------------------------------------------

    /** A null URL takes the fallback branch, which must still be a real, describable node. */
    @Test
    fun profileAvatar_withNoUrl_stillRendersADescribedAvatar() {
        var loadError: Throwable? = null
        composeRule.setContent {
            SmartPhotosTheme {
                ProfileAvatar(profilePictureUrl = null, onImageLoadError = { loadError = it })
            }
        }

        composeRule.onNodeWithContentDescription(string(R.string.cd_profile_picture))
            .assertIsDisplayed()
        // The fallback draws locally, so nothing can fail to load.
        assertNull(loadError)
    }
}
