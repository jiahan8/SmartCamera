/*
 * The Compose screen-test vocabulary: the wait helpers and the resource lookup that every UI suite
 * in the app had its own copy of.
 *
 * Eleven suites declared `string(resId)`, eight of them declared a byte-identical `waitForText`,
 * and the 5s timeout appeared as a literal thirty-three times. That is well past this build's own
 * "one module is a sample size of one -- wait for the second" bar.
 *
 * A separate module rather than a home in :core:testing, for the reason that module's build file
 * records: everything there is `api`, and :app and :core:ui take it too, so the compose-ui-test
 * artifacts these helpers name would land on their classpaths the way Roborazzi once landed on nine
 * features. That is the same split :core:screenshot-testing exists for -- fixtures in one module,
 * a harness in another, each depended on only by what wants it.
 *
 * Worth being precise about what this does *not* cost: the nine feature modules already resolve
 * compose-ui-test on both test source sets, because `smartphotos.android.feature` puts
 * `androidx-ui-test-junit4` there for the screen suites themselves. This module re-exposes those
 * same artifacts and adds no new one to any consumer -- it is our own code being shared, not a
 * toolchain being spread.
 *
 * A regular library module, not AGP's `testFixtures`, on the rule the other two fixtures modules
 * state: the Kotlin Android plugin generates no Kotlin compilation for that variant.
 */
plugins {
    id("smartphotos.android.library")
}

android {
    namespace = "com.jiahan.smartcamera.core.uitesting"
}

dependencies {

    /*
     * api throughout, on the rule the other fixtures modules state: everything this module exposes
     * is a type a consumer names directly. The helpers are extensions on `ComposeTestRule` and
     * `AndroidComposeTestRule`, and a caller writes both types itself, so the test-rule artifact
     * and the BOM pinning it are API surface rather than implementation detail.
     */
    api(libs.junit)

    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui.test.junit4)

    // ComponentActivity, which the rule is parameterised on, and androidx.test's
    // ActivityScenarioRule, which createAndroidComposeRule returns wrapped in -- both appear in
    // `composeTestRule`'s public type, so both are api.
    api(libs.androidx.activity.compose)
    api(libs.androidx.junit)
}