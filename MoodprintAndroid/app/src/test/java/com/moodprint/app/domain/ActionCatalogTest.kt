package com.moodprint.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionCatalogTest {
    @Test
    fun everyActionUsesOneToFiveMinuteTimer() {
        assertEquals(20, ActionCatalog.actions.size)
        assertTrue(ActionCatalog.actions.all { it.durationSeconds in 60..300 })
    }

    @Test
    fun requestedActionsHaveDetailQuestions() {
        val orders = setOf(0, 5, 10, 12, 13, 14, 15)
        assertTrue(ActionCatalog.actions.filter { it.catalogOrder in orders }.all {
            !it.detailPrompt.isNullOrBlank() && !it.detailPlaceholder.isNullOrBlank()
        })
    }
}
