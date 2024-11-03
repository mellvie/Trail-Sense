package com.kylecorry.trail_sense.tools.mirror

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.main.MainActivity
import com.kylecorry.trail_sense.test_utils.AutomationLibrary.isTrue
import com.kylecorry.trail_sense.test_utils.AutomationLibrary.isVisible
import com.kylecorry.trail_sense.test_utils.TestUtils
import com.kylecorry.trail_sense.test_utils.TestUtils.context
import com.kylecorry.trail_sense.test_utils.TestUtils.isCameraInUse
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tools
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ToolMirrorTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val grantPermissionRule = TestUtils.allPermissionsGranted()

    @get:Rule
    val instantExec = InstantTaskExecutorRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        hiltRule.inject()
        TestUtils.setWaitForIdleTimeout()
        TestUtils.setupApplication()
        TestUtils.listenForCameraUsage()
        scenario = TestUtils.startWithTool(Tools.MIRROR_CAMERA)
    }

    @Test
    fun verifyBasicFunctionality() {
        if (!Tools.isToolAvailable(context, Tools.MIRROR_CAMERA)) {
            return
        }

        isVisible(R.id.camera)

        isTrue(10000) {
            isCameraInUse(isBackFacing = false)
        }
    }

    @After
    fun tearDown() {
        TestUtils.stopListeningForCameraUsage()
    }
}