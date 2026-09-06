package com.jiahan.smartcamera.uitest

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Rule

/**
 * How long a screen suite waits for a condition before failing.
 *
 * Compose defaults to 1s, which is short for a screen driven through a real ViewModel -- the
 * debounced search query alone spends 300ms before its fetch starts. All eleven suites had picked
 * 5s independently; this is that number, named once, so changing it is one edit rather than
 * thirty-three.
 */
const val UI_TEST_TIMEOUT_MS = 5_000L

/**
 * Shared setup for the Compose screen suites: the activity-backed test rule, the resource lookup
 * and the four waits every one of them was declaring for itself.
 *
 * Eleven suites held an identical `@get:Rule val composeTestRule`, eleven a `string(resId)`, and
 * eight a byte-identical `waitForText`. This is the `BaseScreenshotTest` arrangement applied to the
 * other test family -- the harness owns the rule, the suite writes `@Test`.
 *
 * A subclass builds its ViewModel from `:core:testing`'s fakes, calls `setContent` through
 * [composeTestRule], and asserts. It does not need a `@Before`: JUnit constructs a new instance
 * per test method, so a suite's fakes and captured navigation flags are already fresh each time.
 *
 * **Wait, do not assert straight after an interaction.** `assertDoesNotExist` immediately after the
 * tap that removes something passes for the wrong reason if the node has not gone yet; the
 * [waitForNoText] / [waitForNoContentDescription] pair is there so that reads as an intent rather
 * than an accident.
 */
abstract class BaseScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    /**
     * Resolves a string resource through the rule's activity.
     *
     * Screen suites assert on product copy by resource rather than by literal, which is what stops
     * a test asserting one screen's string against another's -- the failure mode that left four
     * assertions unable to pass for months. This is the lookup that makes that cheap.
     */
    protected fun string(@StringRes resId: Int): String =
        composeTestRule.activity.getString(resId)

    /** Waits until at least one node displaying [text] exists. */
    protected fun waitForText(text: String) = composeTestRule.waitUntil(UI_TEST_TIMEOUT_MS) {
        composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    /** Waits until no node displaying [text] remains. */
    protected fun waitForNoText(text: String) = composeTestRule.waitUntil(UI_TEST_TIMEOUT_MS) {
        composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
    }

    /** Waits until at least one node with [contentDescription] exists. */
    protected fun waitForContentDescription(contentDescription: String) =
        composeTestRule.waitUntil(UI_TEST_TIMEOUT_MS) {
            composeTestRule.onAllNodesWithContentDescription(contentDescription)
                .fetchSemanticsNodes().isNotEmpty()
        }

    /** Waits until no node with [contentDescription] remains. */
    protected fun waitForNoContentDescription(contentDescription: String) =
        composeTestRule.waitUntil(UI_TEST_TIMEOUT_MS) {
            composeTestRule.onAllNodesWithContentDescription(contentDescription)
                .fetchSemanticsNodes().isEmpty()
        }
}