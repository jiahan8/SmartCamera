package com.jiahan.smartcamera.preview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The one navigation hazard in this module that no other test can reach.
 *
 * Navigation Compose resolves an enum route argument through `Class.forName()` (see `NavType.EnumType`),
 * so R8 renaming or stripping [MediaSourceType] breaks navigation to the two preview screens **in
 * release builds only** — invisible to `assembleDebug`, to every unit test, and to Roborazzi, all of
 * which compile debug. `@Keep` is what prevents it, and an annotation is exactly the kind of line
 * that gets dropped in a refactor with nothing to notice.
 *
 * This asserts the annotation rather than the minified output because that is the part a test can
 * hold: verifying the R8 result would mean building release and reading the mapping file.
 *
 * It reads the compiled class rather than using reflection, and that is forced rather than clever:
 * `androidx.annotation.Keep` is `@Retention(BINARY)`, so `isAnnotationPresent` returns false for a
 * class that carries it. The descriptor is in the constant pool either way, which is the same place
 * R8 looks.
 */
class PreviewRoutesTest {

    @Test
    fun mediaSourceType_isKeptFromR8() {
        val bytecode = MediaSourceType::class.java
            .getResourceAsStream("MediaSourceType.class")
            .use { requireNotNull(it) { "MediaSourceType.class not on the test classpath" }.readBytes() }

        assertTrue(
            "MediaSourceType must stay @Keep: Navigation resolves enum route arguments by name " +
                    "via Class.forName(), so R8 renaming it breaks preview navigation in release only.",
            String(bytecode, Charsets.ISO_8859_1).contains("Landroidx/annotation/Keep;"),
        )
    }

    /**
     * The constant names are the wire format. Navigation serializes an enum argument by `name`, so
     * renaming a constant changes the route a saved back stack refers to.
     */
    @Test
    fun mediaSourceType_constantNamesAreStable() {
        assertEquals(
            listOf("LOCAL", "REMOTE"),
            MediaSourceType.entries.map { it.name },
        )
    }
}
