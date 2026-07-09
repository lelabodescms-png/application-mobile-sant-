package com.labodescms.santetracker

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Captures one screenshot per tab for the Play Store listing. Not a correctness test —
 * run manually via the "Android Screenshots" CI workflow, which pulls the PNGs off the
 * emulator afterwards. Bottom-nav icons are targeted by content description (each tab's
 * label) since screen titles like "Poids" also appear as on-screen card labels.
 *
 * Screenshots are taken with the shell `screencap` binary (via UiDevice.executeShellCommand)
 * straight to /sdcard, rather than UiDevice.takeScreenshot() into the app's external files
 * dir — that path didn't resolve consistently across CI emulator images.
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun capture(name: String) {
        composeTestRule.waitForIdle()
        Thread.sleep(400)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("screencap -p /sdcard/$name.png")
    }

    @Test
    fun captureAllScreens() {
        capture("01_accueil")

        composeTestRule.onNodeWithContentDescription("Poids").performClick()
        capture("02_poids")

        composeTestRule.onNodeWithContentDescription("Journal").performClick()
        capture("03_journal")

        composeTestRule.onNodeWithContentDescription("Réglages").performClick()
        capture("04_reglages")
    }
}
