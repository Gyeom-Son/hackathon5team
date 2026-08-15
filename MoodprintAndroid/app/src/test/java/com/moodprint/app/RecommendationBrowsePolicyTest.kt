package com.moodprint.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecommendationBrowsePolicyTest {
    @Test fun nextRecommendation_wrapsWithinPresentedCandidates() {
        val candidates = listOf("a", "b", "c", "d", "e")

        assertEquals("b", nextRecommendationId(candidates, "a"))
        assertEquals("a", nextRecommendationId(candidates, "e"))
    }

    @Test fun unknownCurrent_startsAtFirstCandidate() {
        assertEquals("a", nextRecommendationId(listOf("a", "b"), "missing"))
    }

    @Test fun emptyCandidates_haveNoNextRecommendation() {
        assertNull(nextRecommendationId(emptyList(), "missing"))
    }
}
