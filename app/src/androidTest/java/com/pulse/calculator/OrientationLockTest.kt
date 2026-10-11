package com.pulse.calculator

import android.content.ComponentName
import android.content.pm.ActivityInfo
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

/** Landscape is out of scope for v1 (PRODUCT_SPEC section 11), and its layout breaks the 48 dp touch targets. */
class OrientationLockTest {

    @Test
    fun theActivityIsLockedToPortrait() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val info = context.packageManager.getActivityInfo(ComponentName(context, MainActivity::class.java), 0)

        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, info.screenOrientation)
    }
}
