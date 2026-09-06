package com.jiahan.smartcamera.navigation

import android.app.Application
import android.content.Intent
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jiahan.smartcamera.MainActivity
import com.jiahan.smartcamera.search.SearchRoute.SEARCH_DEEP_LINK_URI_PATTERN
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The half of the search deep link that lives outside the nav graph.
 *
 * **`live://jiahan8.github.io/search` is written down twice**, and nothing but this file connects
 * the two. `SearchRoute.SEARCH_DEEP_LINK_URI_PATTERN` is one whole URI that
 * `smartPhotosNavGraph` hands to `navDeepLink`; `AndroidManifest.xml` spells the same URI as three
 * separate attributes on an intent filter (`scheme`, `host`, `pathPrefix`). Change either side and
 * the other still compiles, still lints, and still passes every other test in the repo -- the link
 * just stops working, and it stops working in the one way nobody exercises by hand, because it only
 * ever arrives from outside the app.
 *
 * The failure is asymmetric, which is why both halves are worth pinning:
 *
 * - Manifest narrowed or removed, graph intact — the system never routes the URI here at all. The
 *   tap opens a browser, or nothing. This test is the only thing that would notice.
 * - Manifest intact, graph pattern changed — the app *launches* and lands on its start destination
 *   instead of Search, which reads as "the link is a bit broken" rather than as a failure.
 *   `SmartPhotosNavigationTest.searchDeepLink_navigatesToSearch` covers that side.
 *
 * A JVM test rather than an instrumented one because a merged manifest is a build artifact, not a
 * device behaviour: Robolectric resolves the intent against the same merged manifest the APK ships,
 * so this reports in seconds and in the fast CI job. It asserts the *declaration*, not that
 * Android's real chooser would honour it.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class SearchDeepLinkManifestTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    /** The intent a browser or another app fires at this URI. */
    private fun viewIntent(uri: String) = Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        addCategory(Intent.CATEGORY_DEFAULT)
    }

    private fun resolvedActivityNames(uri: String) =
        context.packageManager.queryIntentActivities(viewIntent(uri), 0)
            .map { it.activityInfo.name }

    @Test
    fun manifest_advertisesTheUriSearchRouteDeclares() {
        val resolved = resolvedActivityNames(SEARCH_DEEP_LINK_URI_PATTERN)

        assertTrue(
            "no activity in the merged manifest handles $SEARCH_DEEP_LINK_URI_PATTERN -- " +
                    "SearchRoute and AndroidManifest.xml have drifted apart",
            resolved.contains(MainActivity::class.java.name),
        )
    }

    /**
     * Deep links land on `MainActivity` and only `MainActivity`.
     *
     * It is the sole `@AndroidEntryPoint` activity and the only host of the NavHost, so a second
     * activity claiming this URI would mean a chooser dialog for what should be a direct open.
     */
    @Test
    fun onlyMainActivityClaimsTheSearchDeepLink() {
        assertEquals(
            listOf(MainActivity::class.java.name),
            resolvedActivityNames(SEARCH_DEEP_LINK_URI_PATTERN),
        )
    }

    /**
     * The filter is scoped to the path it means, not to the whole host.
     *
     * Without this the suite above passes just as well against `android:pathPrefix="/"` or a
     * missing `pathPrefix` -- an app that claims every `live://jiahan8.github.io` URI, including
     * the `/image` path that is a *separate* filter and every path nobody has written yet. That is
     * the shape a "fix" to a broken deep link tends to take.
     */
    @Test
    fun manifest_doesNotClaimAnUnrelatedPathOnTheSameHost() {
        assertEquals(
            emptyList<String>(),
            resolvedActivityNames("live://jiahan8.github.io/not-a-registered-path"),
        )
    }
}
