package io.github.humayanx.lastdrop.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationPreviewTest {

    @Test
    fun stripsRedundantSenderPrefixMatchingTitle() {
        val result = formatNotificationBodyPreview(
            content = "David: Are we meeting at the coffee shop at 4?",
            bigText = null,
            title = "David"
        )
        assertEquals("Are we meeting at the coffee shop at 4?", result)
    }

    @Test
    fun stripsSenderPrefixWhenTitleHasChannelSeparator() {
        val result = formatNotificationBodyPreview(
            content = "Sarah Chen: Updated the Figma mockups!",
            bigText = null,
            title = "#mobile-team · Sarah Chen"
        )
        assertEquals("Updated the Figma mockups!", result)
    }

    @Test
    fun collapsesMultipleNewlinesAndWhitespace() {
        val result = formatNotificationBodyPreview(
            content = "Line 1\n\n\nLine 2   with   extra   spaces\nLine 3",
            bigText = null,
            title = "Notification Title"
        )
        assertEquals("Line 1 Line 2 with extra spaces Line 3", result)
    }

    @Test
    fun avoidsDuplicateTitleWhenContentEqualsTitle() {
        val result = formatNotificationBodyPreview(
            content = "System Update Ready",
            bigText = "Android 15 is now ready to download and install on your device.",
            title = "System Update Ready"
        )
        assertEquals("Android 15 is now ready to download and install on your device.", result)
    }

    @Test
    fun formatsChatLogWithBullets() {
        val result = formatNotificationBodyPreview(
            content = "Alice: Are you ready?\nBob: Almost ready!",
            bigText = null,
            title = "Project Chat"
        )
        assertEquals("Alice: Are you ready?  •  Bob: Almost ready!", result)
    }
}
