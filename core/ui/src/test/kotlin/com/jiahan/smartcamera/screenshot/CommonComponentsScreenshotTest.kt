package com.jiahan.smartcamera.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jiahan.smartcamera.common.BottomSheetActionItem
import com.jiahan.smartcamera.common.CustomSnackbarHost
import com.jiahan.smartcamera.common.DeleteNoteConfirmationDialog
import com.jiahan.smartcamera.common.FullScreenMessage
import com.jiahan.smartcamera.common.NoteListSkeleton
import com.jiahan.smartcamera.common.PasswordField
import com.jiahan.smartcamera.common.ProfileAvatar
import com.jiahan.smartcamera.common.SearchBar
import com.jiahan.smartcamera.common.shimmer
import com.jiahan.smartcamera.common.showAppSnackbar
import com.jiahan.smartcamera.ui.theme.SmartPhotosTheme
import org.junit.Test

/**
 * Goldens for the shared vocabulary in `common/`, which had none.
 *
 * [com.jiahan.smartcamera.common.NoteItem] was the only composable in this module with a golden, and
 * it is the one every feature draws *through* rather than *with*. The components here are the ones
 * features compose directly — every empty and error state in the app is a [FullScreenMessage], every
 * avatar a [ProfileAvatar] — so a regression in one of them is app-wide rather than screen-local,
 * and no feature-level test would localise it.
 *
 * Deterministic by construction: no remote URLs anywhere, so Coil performs no I/O. [ProfileAvatar]
 * is captured with a null URL on purpose — that is the fallback branch, and the only one a test can
 * render without the network.
 */
class CommonComponentsScreenshotTest : BaseScreenshotTest() {

    /** Every capture sits on the background the app actually paints, as the screen goldens do. */
    private fun captureThemed(darkTheme: Boolean, content: @Composable () -> Unit) {
        capture {
            SmartPhotosTheme(darkTheme = darkTheme) {
                Surface(color = MaterialTheme.colorScheme.background) { content() }
            }
        }
    }

    // -------------------------------------------------------------------------
    // FullScreenMessage — every empty and error state in the app
    // -------------------------------------------------------------------------

    @Test
    fun fullScreenMessage_light() = captureThemed(false) { FullScreenMessage("No results found") }

    @Test
    fun fullScreenMessage_dark() = captureThemed(true) { FullScreenMessage("No results found") }

    // -------------------------------------------------------------------------
    // ProfileAvatar — the null-URL fallback, drawn wherever a user has no picture
    // -------------------------------------------------------------------------

    @Test
    fun profileAvatar_fallback_light() =
        captureThemed(false) { ProfileAvatar(profilePictureUrl = null, onImageLoadError = {}) }

    @Test
    fun profileAvatar_fallback_dark() =
        captureThemed(true) { ProfileAvatar(profilePictureUrl = null, onImageLoadError = {}) }

    // -------------------------------------------------------------------------
    // SearchBar — empty and filled, since the container colour animates on focus
    // -------------------------------------------------------------------------

    @Test
    fun searchBar_empty_light() = captureThemed(false) { SampleSearchBar("") }

    @Test
    fun searchBar_empty_dark() = captureThemed(true) { SampleSearchBar("") }

    @Test
    fun searchBar_withQuery_light() = captureThemed(false) { SampleSearchBar("mountains") }

    @Test
    fun searchBar_withQuery_dark() = captureThemed(true) { SampleSearchBar("mountains") }

    // -------------------------------------------------------------------------
    // PasswordField — both halves of the visibility toggle, plus the error arm
    // -------------------------------------------------------------------------

    @Test
    fun passwordField_hidden_light() = captureThemed(false) { SamplePasswordField(visible = false) }

    @Test
    fun passwordField_hidden_dark() = captureThemed(true) { SamplePasswordField(visible = false) }

    @Test
    fun passwordField_visible_light() = captureThemed(false) { SamplePasswordField(visible = true) }

    /**
     * The only state that draws the password as plaintext, so it is the only one where a colour
     * regression on the revealed value can show -- the hidden and error goldens both mask it, and
     * their pixels would not move.
     */
    @Test
    fun passwordField_visible_dark() = captureThemed(true) { SamplePasswordField(visible = true) }

    @Test
    fun passwordField_error_light() =
        captureThemed(false) { SamplePasswordField(visible = false, error = "Password is too short") }

    @Test
    fun passwordField_error_dark() =
        captureThemed(true) { SamplePasswordField(visible = false, error = "Password is too short") }

    // -------------------------------------------------------------------------
    // Dialog and sheet row
    // -------------------------------------------------------------------------

    @Test
    fun deleteNoteConfirmationDialog_light() =
        captureThemed(false) { DeleteNoteConfirmationDialog({}, {}) }

    @Test
    fun deleteNoteConfirmationDialog_dark() =
        captureThemed(true) { DeleteNoteConfirmationDialog({}, {}) }

    @Test
    fun bottomSheetActionItems_light() = captureThemed(false) { SampleSheetRows() }

    @Test
    fun bottomSheetActionItems_dark() = captureThemed(true) { SampleSheetRows() }

    // -------------------------------------------------------------------------
    // Shimmer — the loading skeleton's building block
    // -------------------------------------------------------------------------

    @Test
    fun shimmer_light() = captureThemed(false) { SampleShimmer() }

    @Test
    fun shimmer_dark() = captureThemed(true) { SampleShimmer() }

    // -------------------------------------------------------------------------
    // NoteListSkeleton -- what all four mirrored lists draw before their first page
    // -------------------------------------------------------------------------

    @Test
    fun noteListSkeleton_light() = captureThemed(false) { NoteListSkeleton() }

    @Test
    fun noteListSkeleton_dark() = captureThemed(true) { NoteListSkeleton() }

    // -------------------------------------------------------------------------
    // CustomSnackbarHost -- both arms, since only the error one reads errorContainer
    // -------------------------------------------------------------------------

    @Test
    fun snackbar_light() = captureThemed(false) { SampleSnackbar(isError = false) }

    @Test
    fun snackbar_dark() = captureThemed(true) { SampleSnackbar(isError = false) }

    @Test
    fun snackbar_error_light() = captureThemed(false) { SampleSnackbar(isError = true) }

    @Test
    fun snackbar_error_dark() = captureThemed(true) { SampleSnackbar(isError = true) }

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    @Composable
    private fun SampleSearchBar(query: String) {
        SearchBar(searchQuery = query, onSearchQueryChange = {}) { Text("Search notes") }
    }

    @Composable
    private fun SamplePasswordField(visible: Boolean, error: String? = null) {
        PasswordField(
            value = "hunter2000",
            onValueChange = {},
            label = "Password",
            visible = visible,
            onVisibilityChange = {},
            modifier = Modifier.padding(16.dp),
            errorMessage = error,
        )
    }

    @Composable
    private fun SampleSheetRows() {
        Column {
            BottomSheetActionItem(Icons.Default.Share, "Share", {})
            BottomSheetActionItem(
                Icons.Default.Delete,
                "Delete",
                {},
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }

    @Composable
    private fun SampleShimmer() {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.fillMaxWidth(0.45f).height(14.dp).shimmer())
        }
    }

    /**
     * A snackbar held on screen for the capture.
     *
     * `Indefinite` is load-bearing rather than cosmetic: `showAppSnackbar` defaults an
     * action-less snackbar to `Short`, and `waitForIdle` advances the test clock far enough to
     * dismiss one -- so the golden would record an empty host, pass forever, and assert nothing.
     * The call suspends until a dismissal that never comes, which leaves the coroutine parked
     * rather than the composition busy.
     */
    @Composable
    private fun SampleSnackbar(isError: Boolean) {
        val hostState = remember { SnackbarHostState() }
        LaunchedEffect(isError) {
            hostState.showAppSnackbar(
                message = if (isError) "Couldn't delete the note" else "Note added",
                isError = isError,
                duration = SnackbarDuration.Indefinite,
            )
        }
        CustomSnackbarHost(hostState)
    }
}
