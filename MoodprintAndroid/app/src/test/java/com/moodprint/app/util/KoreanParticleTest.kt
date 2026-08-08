package com.moodprint.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class KoreanParticleTest {
    @Test fun objectParticleAddsEulAfterBatchim() {
        assertEquals("무기력을", KoreanParticle.withObjectParticle("무기력"))
        assertEquals("슬픔을", KoreanParticle.withObjectParticle("슬픔"))
    }

    @Test fun objectParticleAddsReulWithoutBatchim() {
        assertEquals("가볍게 몸 풀기를", KoreanParticle.withObjectParticle("가볍게 몸 풀기"))
    }

    @Test fun topicParticleRespectsBatchim() {
        assertEquals("불안은", KoreanParticle.withTopicParticle("불안"))
        assertEquals("복잡함은", KoreanParticle.withTopicParticle("복잡함"))
    }

    @Test fun subjectParticleRespectsBatchim() {
        assertEquals("외로움이", KoreanParticle.withSubjectParticle("외로움"))
        assertEquals("화남이", KoreanParticle.withSubjectParticle("화남"))
    }

    @Test fun nonHangulWordIsReturnedUnchanged() {
        assertEquals("MVP", KoreanParticle.withObjectParticle("MVP"))
    }
}
