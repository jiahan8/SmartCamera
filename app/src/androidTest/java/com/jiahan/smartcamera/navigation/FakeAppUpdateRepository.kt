package com.jiahan.smartcamera.navigation

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.jiahan.smartcamera.data.repository.AppUpdateRepository
import com.jiahan.smartcamera.domain.AppUpdateState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * [AppUpdateRepository] double, reporting that no update is available.
 *
 * Local to this source set rather than a tenth entry in `:core:testing`, and the reason is a module
 * rule rather than convenience: `AppUpdateRepository` is one of the two interfaces that could not
 * move to `:core:domain` because its signature carries Android types, so it stays in
 * `:core:data` -- and **neither fixtures module may depend on `:core:data`.** A fake there would
 * drag that edge in.
 *
 * Nothing in `SmartPhotosApp` asks for it; only `MainViewModel` injects it, and the nav test hosts
 * the composable directly. It exists because uninstalling `DataModule` takes all nine of its
 * bindings with it, and Hilt validates `MainViewModel`'s dependencies whether or not this test
 * ever constructs one.
 */
class FakeAppUpdateRepository : AppUpdateRepository {

    override fun observeUpdateState(): Flow<AppUpdateState> = flowOf(AppUpdateState.NotAvailable)

    override fun startFlexibleUpdate(
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ): Boolean = false

    override suspend fun completeUpdate(): Result<Unit> = Result.success(Unit)
}
