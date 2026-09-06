package com.jiahan.smartcamera.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

/**
 * A real [HttpsCallableResult] carrying [payload], built reflectively.
 *
 * Two things rule out the obvious approaches. `every { result.data }` never records a call -- the
 * SDK exposes `data` as a public field alongside its getter, so Kotlin reads the field directly and
 * mockk sees nothing. Calling the constructor is a compile error, because it is `internal` in
 * Kotlin even though bytecode marks it public -- which is also why reflection reaches it without
 * `setAccessible`.
 *
 * Worth the awkwardness: the payload then arrives at the reader exactly as the SDK hands it over,
 * which is the whole point of testing an untyped `Map` cast.
 */
internal fun httpsResult(payload: Any?): HttpsCallableResult =
    HttpsCallableResult::class.java
        .getDeclaredConstructor(Any::class.java)
        .newInstance(payload)

/**
 * Stubs every callable on [functions] to succeed with [payload] as its `data`, returning the slot
 * that captures which function name was actually asked for.
 *
 * The capture is what stops a whole file of payload tests from being blind to the wire name. These
 * stubs answer *any* name with the same payload -- which is what makes them reusable, and also
 * meant that swapping two function-name constants left every assertion here green. Assert
 * `.captured` in at least one test per callable; see `isUsernameAvailable calls the ... function`.
 */
internal fun stubCallable(functions: FirebaseFunctions, payload: Any?): CapturingSlot<String> {
    val nameSlot = slot<String>()
    val callable: HttpsCallableReference = mockk()
    every { callable.call(any()) } returns Tasks.forResult(httpsResult(payload))
    every { functions.getHttpsCallable(capture(nameSlot)) } returns callable
    return nameSlot
}

/** Stubs every callable on [functions] to fail with [error]. */
internal fun stubCallableFailure(functions: FirebaseFunctions, error: Exception) {
    val callable: HttpsCallableReference = mockk()
    every { callable.call(any()) } returns Tasks.forException(error)
    every { functions.getHttpsCallable(any()) } returns callable
}