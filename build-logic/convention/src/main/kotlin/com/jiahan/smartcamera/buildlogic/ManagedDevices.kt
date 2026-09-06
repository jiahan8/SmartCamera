package com.jiahan.smartcamera.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

/**
 * The emulator CI runs the instrumented tests on, declared in Gradle so nobody has to have one.
 *
 * **This exists because `compileDebugAndroidTestKotlin` was the whole of CI's androidTest story.**
 * Compiling is not running: 103 instrumented tests across eleven modules, and the only thing that
 * ever executed them was somebody remembering to attach a device or run `./scripts/run-test-lab.sh`
 * by hand. Most of them are `sharedTest/` suites that Robolectric also runs on the JVM, so CI did
 * see their assertions -- but fourteen are androidTest-only, and those fourteen are the ones with
 * no JVM half to fall back on: `HiltGraphSmokeTest` (the only check that the real Hilt graph
 * assembles), `SmartPhotosNavigationTest` (the only check that the nav graph works), and the two
 * suites that are device-only because production code sleeps on `Dispatchers.Main`. This build has
 * already paid for that gap once -- four `sharedTest` assertions sat wrong for months because
 * nothing ran them -- and the four that remain are exactly the ones a wrong assertion would hide
 * in.
 *
 * On every Android module rather than on `:app`: eleven modules have androidTest sources and all
 * eleven want the same device. A module without them gets a task that runs nothing, which costs a
 * task name.
 *
 * ### Why this device
 *
 * - **API 36**, the app's `targetSdk`, not the oldest API that would boot. The one device-only
 *   failure this repo has actually recorded was API-36-specific: espresso-core 3.5.0 reaching for
 *   `InputManager.getInstance`, removed in 36, which killed 51 tests across seven modules in
 *   `onIdle` before their first assertion. A CI emulator pinned to something older would have
 *   watched all of that go green. `minSdk` 28 is a compile floor, not a test target.
 * - **`google-atd`**, the Automated Test Device image: no Play Store, no system apps, no
 *   animations, roughly half the boot time and memory of a full image, and it is what Google
 *   recommends for exactly this job. `aosp-atd` is smaller still and is the wrong choice here --
 *   `HiltGraphSmokeTest` resolves the real `AppUpdateManager` and lets Firebase auto-initialise, so
 *   the Google APIs have to be present. Both variants publish `x86_64` and `arm64-v8a` at this API,
 *   so the same declaration serves CI and an Apple-silicon laptop.
 *
 * The device name is the task name: `pixel6Api36DebugAndroidTest`. Renaming it changes the CI
 * workflow's command.
 */
internal fun Project.configureManagedDevices(
    commonExtension: CommonExtension,
) {
    with(commonExtension.testOptions) {
        /*
         * Sets the three system animation scales to 0 for the duration of a run, the same thing
         * the testing docs tell you to do by hand in Developer options. A Compose rule syncs on
         * its own clock and does not strictly need it, but the navigation transitions and the
         * bottom bar's spring are real window animations that the *platform* drives, and those are
         * what leave a tap landing on a view that has not settled.
         */
        animationsDisabled = true

        managedDevices.localDevices.create("pixel6Api36").apply {
            device = "Pixel 6"
            apiLevel = 36
            systemImageSource = "google-atd"
        }
    }
}
