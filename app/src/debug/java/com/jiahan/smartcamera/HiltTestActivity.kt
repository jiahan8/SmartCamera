package com.jiahan.smartcamera

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * An empty `@AndroidEntryPoint` host for Compose tests that render Hilt-injected screens.
 *
 * [SmartPhotosApp] takes its state as parameters, but the screens inside its nav graph default
 * their ViewModel to `hiltViewModel()`, which resolves through the *activity* rather than the
 * application. A plain `ComponentActivity` -- including the one `ui-test-manifest` contributes --
 * is not a Hilt entry point, so `createAndroidComposeRule<ComponentActivity>()` fails the moment
 * the graph composes its first screen. This is the smallest activity that works.
 *
 * It lives in `src/debug` rather than `src/androidTest` because an activity has to be in the
 * manifest of the app under test for the instrumentation to launch it, and only a variant source
 * set merges into that. `androidx.compose.ui:ui-test-manifest` contributes its `ComponentActivity`
 * the same way, as a `debugImplementation`, so this is the existing arrangement rather than a new
 * one -- and like that one it cannot reach release, which has no `src/debug` to merge.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
