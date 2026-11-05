package com.studysync.sa

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studysync.sa.ui.screens.WelcomeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainAppUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun welcomeScreen_displaysTitleAndGetStartedButton() {
        var clicked = false
        composeTestRule.setContent {
            WelcomeScreen(onGetStarted = { clicked = true })
        }

        composeTestRule.onNodeWithText("Get Started", substring = true).performClick()
        assert(clicked)
    }
}
