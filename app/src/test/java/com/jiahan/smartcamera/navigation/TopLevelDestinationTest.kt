package com.jiahan.smartcamera.navigation

import com.jiahan.smartcamera.favorite.FavoriteRoute
import com.jiahan.smartcamera.home.HomeRoute
import com.jiahan.smartcamera.note.NoteRoute
import com.jiahan.smartcamera.profile.ProfileRoute
import com.jiahan.smartcamera.search.SearchRoute
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the one thing [TopLevelDestination]'s design cannot check for itself.
 *
 * The enum closes the *set* — a private constructor means nothing outside can add a sixth tab — but
 * [TopLevelDestination.route] is typed `Any`, because the route types share no supertype and cannot
 * (Kotlin requires a sealed type's subtypes in the same module, which a `:feature:*` split rules
 * out). So the compiler will accept literally any value there, and a route that Navigation cannot
 * serialize fails at runtime by simply never matching a destination — a tab that silently does
 * nothing, with no crash and no compile error.
 *
 * That is what the first test below covers. The rest pin the metadata that is a decision rather
 * than a restatement: which tabs scroll to top, and that no two tabs point at the same destination.
 */
class TopLevelDestinationTest {

    /**
     * Every route must carry `@Serializable`, which is what Navigation Compose's type-safe API
     * needs to build a destination pattern from it.
     *
     * `@Serializable` is `@Retention(RUNTIME)`, so unlike `@Keep` this one is reflectable.
     */
    @Test
    fun everyRouteIsSerializable() {
        val notSerializable = TopLevelDestination.entries.filterNot { destination ->
            destination.route::class.java.isAnnotationPresent(Serializable::class.java)
        }

        assertTrue(
            "Routes are typed `Any`, so a non-@Serializable one compiles and then never matches a " +
                    "destination at runtime: $notSerializable",
            notSerializable.isEmpty(),
        )
    }

    /** Two tabs pointing at one destination would make the bottom bar's selection ambiguous. */
    @Test
    fun everyDestinationHasADistinctRoute() {
        val routes = TopLevelDestination.entries.map { it.route }

        assertEquals(routes.size, routes.distinct().size)
    }

    /**
     * The bottom bar is five tabs in this order. Pinned because the order is what the user sees and
     * nothing else asserts it — a reorder is a silent UI change.
     */
    @Test
    fun theBottomBarIsTheseFiveInThisOrder() {
        assertEquals(
            listOf(HomeRoute, SearchRoute, NoteRoute, FavoriteRoute, ProfileRoute),
            TopLevelDestination.entries.map { it.route },
        )
    }

    /**
     * Re-tapping a tab scrolls its list to the top only where there is a list to scroll. Note and
     * Profile are forms, so the gesture would have nowhere to go.
     */
    @Test
    fun onlyTheListTabsScrollToTop() {
        assertEquals(
            listOf(
                TopLevelDestination.HOME,
                TopLevelDestination.SEARCH,
                TopLevelDestination.FAVORITE,
            ),
            TopLevelDestination.entries.filter { it.scrollsToTop },
        )
    }

    /** Every tab needs a label; a zero resource id is the shape a missing one takes. */
    @Test
    fun everyDestinationHasATitleResource() {
        assertTrue(TopLevelDestination.entries.all { it.titleResId != 0 })
    }
}
