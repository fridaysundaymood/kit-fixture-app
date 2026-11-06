package dev.androidkit.fixture

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class TaskboardTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun boardAndTaskEntryAreAccessible() {
        compose.waitUntil(10_000) { compose.onAllNodesWithText("General").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("General").performClick()
        compose.onNodeWithText("Add task").performClick()
        compose.onNodeWithText("Task title").performTextInput("Review checklist")
        compose.onNodeWithText("Save").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Review checklist").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Review checklist").assertIsDisplayed()
    }
}
