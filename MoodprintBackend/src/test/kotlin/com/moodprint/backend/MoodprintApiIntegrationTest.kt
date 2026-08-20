package com.moodprint.backend

import org.hamcrest.Matchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.delete
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.assertThrows
import com.moodprint.backend.config.SessionRateLimiter
import com.moodprint.backend.service.RateLimited

@SpringBootTest @AutoConfigureMockMvc
class MoodprintApiIntegrationTest @Autowired constructor(
    private val mvc: MockMvc,
) {
    private fun token(): String {
        val body = mvc.post("/api/v1/anonymous-sessions").andExpect { status { isOk() } }.andReturn().response.contentAsString
        return Regex("\\\"token\\\":\\\"([^\\\"]+)\\\"").find(body)!!.groupValues[1]
    }

    @Test
    fun `client mood id and action completion are idempotent while reward is applied once`() {
        val token = token()
        val moodId = UUID.randomUUID()
        val moodBody = """{"clientMoodId":"$moodId","emotions":["ANXIOUS"],"energy":"LOW","recordedDate":"${LocalDate.now()}"}"""
        repeat(2) {
            mvc.post("/api/v1/moods") { header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON; content = moodBody }
                .andExpect { status { isOk() }; jsonPath("$.id") { value(moodId.toString()) } }
        }
        val sessionId = UUID.randomUUID()
        val completion = """{"sessionId":"$sessionId","moodId":"$moodId","actionId":"17E3A608-17F8-4BEA-94B3-370DFBF82D01","change":null}"""
        mvc.post("/api/v1/action-completions") { header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON; content = completion }
            .andExpect { status { isOk() }; jsonPath("$.reward.experienceAwarded") { value(15) }; jsonPath("$.reward.alreadyApplied") { value(false) } }
        mvc.post("/api/v1/action-completions") { header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON; content = completion }
            .andExpect { status { isOk() }; jsonPath("$.reward.alreadyApplied") { value(true) }; jsonPath("$.pets[0].experience") { value(15) } }
    }

    @Test
    fun `collection rewards follow the same canonical order shown by the app`() {
        val token = token()
        repeat(6) {
            val moodId = UUID.randomUUID()
            mvc.post("/api/v1/moods") {
                header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON
                content = """{"clientMoodId":"$moodId","emotions":["ANXIOUS"],"energy":"LOW","recordedDate":"${LocalDate.now()}"}"""
            }.andExpect { status { isOk() } }
            mvc.post("/api/v1/action-completions") {
                header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON
                content = """{"sessionId":"${UUID.randomUUID()}","moodId":"$moodId","actionId":"17E3A608-17F8-4BEA-94B3-370DFBF82D01"}"""
            }.andExpect { status { isOk() } }
        }
        mvc.get("/api/v1/pets") { header("Authorization", "Bearer $token") }.andExpect {
            status { isOk() }
            jsonPath("$[1].petKey") { value("dog") }
            jsonPath("$[1].unlocked") { value(true) }
            jsonPath("$[1].fragments") { value(5) }
            jsonPath("$[2].petKey") { value("rabbit") }
            jsonPath("$[2].unlocked") { value(false) }
            jsonPath("$[2].fragments") { value(1) }
        }
    }

    @Test
    fun `owner isolation validation catalog recommendation pets and monthly records work`() {
        val owner = token()
        val other = token()
        val moodId = UUID.randomUUID()
        val body = """{"clientMoodId":"$moodId","emotions":["UPSET","LONELY"],"energy":"LOW","note":"오늘 기록","recordedDate":"${LocalDate.now()}"}"""
        mvc.post("/api/v1/moods") { header("Authorization", "Bearer $owner"); contentType = MediaType.APPLICATION_JSON; content = body }.andExpect { status { isOk() } }
        mvc.get("/api/v1/moods") { header("Authorization", "Bearer $other") }.andExpect { status { isOk() }; jsonPath("$") { isEmpty() } }
        mvc.get("/api/v1/actions").andExpect { status { isOk() }; jsonPath("$", hasSize<Any>(20)) }
        mvc.post("/api/v1/recommendations") {
            header("Authorization", "Bearer $owner"); contentType = MediaType.APPLICATION_JSON
            content = """{"emotions":["UPSET","LONELY"],"energy":"LOW"}"""
        }.andExpect { status { isOk() }; jsonPath("$[0].reason", not(containsString("비슷한 기록"))) }
        mvc.get("/api/v1/pets") { header("Authorization", "Bearer $owner") }.andExpect {
            status { isOk() }
            jsonPath("$", hasSize<Any>(16))
            jsonPath("$[0].petKey") { value("cat") }
            jsonPath("$[1].petKey") { value("dog") }
            jsonPath("$[2].petKey") { value("rabbit") }
            jsonPath("$[15].petKey") { value("chick") }
        }
        mvc.get("/api/v1/records/monthly") { header("Authorization", "Bearer $owner"); param("year", LocalDate.now().year.toString()); param("month", LocalDate.now().monthValue.toString()) }
            .andExpect { status { isOk() }; jsonPath("$.stats.checkInCount") { value(1) }; jsonPath("$.records[0].note") { value("오늘 기록") } }
    }

    @Test
    fun `invalid emotion count future date and missing token are rejected`() {
        val token = token()
        mvc.post("/api/v1/moods") {
            header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON
            content = """{"emotions":[],"energy":"LOW","recordedDate":"${LocalDate.now().plusDays(1)}"}"""
        }.andExpect { status { isBadRequest() } }
        mvc.post("/api/v1/moods") {
            header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON
            content = """{"emotions":["ANXIOUS"],"energy":"LOW","recordedDate":"${LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1)}"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value("미래 날짜에는 기록할 수 없어요.") }
        }
        mvc.get("/api/v1/moods").andExpect { status { isUnauthorized() } }
        mvc.post("/api/v1/moods") {
            header("Authorization", "Bearer $token"); header("X-Request-ID", "contract-test")
            contentType = MediaType.APPLICATION_JSON; content = "{not-json"
        }.andExpect {
            status { isBadRequest() }
            header { string("X-Request-ID", "contract-test") }
            jsonPath("$.code") { value("INVALID_REQUEST") }
            jsonPath("$.requestId") { value("contract-test") }
        }
    }

    @Test
    fun `idempotency keys reject a different canonical payload`() {
        val token = token()
        val moodId = UUID.randomUUID()
        fun mood(emotion: String) = """{"clientMoodId":"$moodId","emotions":["$emotion"],"energy":"LOW","note":" note ","recordedDate":"${LocalDate.now()}"}"""
        mvc.post("/api/v1/moods") { header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON; content = mood("ANXIOUS") }
            .andExpect { status { isOk() } }

        val sessionId = UUID.randomUUID()
        fun completion(action: String) = """{"sessionId":"$sessionId","moodId":"$moodId","actionId":"$action","change":null}"""
        mvc.post("/api/v1/action-completions") { header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON; content = completion("17E3A608-17F8-4BEA-94B3-370DFBF82D01") }
            .andExpect { status { isOk() } }
        mvc.post("/api/v1/moods") { header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON; content = mood("SAD") }
            .andExpect { status { isConflict() }; jsonPath("$.code") { value("CONFLICT") } }
        mvc.post("/api/v1/action-completions") { header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON; content = completion("17E3A608-17F8-4BEA-94B3-370DFBF82D02") }
            .andExpect { status { isConflict() } }
    }

    @Test
    fun `unfinished mood can be edited with same client id but completed mood is immutable`() {
        val token = token()
        val moodId = UUID.randomUUID()
        fun mood(emotion: String, note: String) =
            """{"clientMoodId":"$moodId","emotions":["$emotion"],"energy":"LOW","note":"$note","recordedDate":"${LocalDate.now()}"}"""

        mvc.post("/api/v1/moods") { header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON; content = mood("ANXIOUS", "first") }
            .andExpect { status { isOk() } }
        mvc.post("/api/v1/moods") { header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON; content = mood("LONELY", "edited") }
            .andExpect { status { isOk() }; jsonPath("$.id") { value(moodId.toString()) }; jsonPath("$.note") { value("edited") } }

        mvc.post("/api/v1/action-completions") {
            header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON
            content = """{"sessionId":"${UUID.randomUUID()}","moodId":"$moodId","actionId":"17E3A608-17F8-4BEA-94B3-370DFBF82D01"}"""
        }.andExpect { status { isOk() } }
        mvc.post("/api/v1/moods") { header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON; content = mood("SAD", "late edit") }
            .andExpect { status { isConflict() } }
    }

    @Test
    fun `oversized API write is rejected before JSON parsing`() {
        val body = "{\"padding\":\"${"x".repeat(17_000)}\"}"
        mvc.post("/api/v1/anonymous-sessions") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isContentTooLarge() }
            jsonPath("$.code") { value("PAYLOAD_TOO_LARGE") }
        }
    }

    @Test
    fun `deleting me removes records and invalidates the bearer token`() {
        val token = token()
        val moodId = UUID.randomUUID()
        mvc.post("/api/v1/moods") {
            header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON
            content = """{"clientMoodId":"$moodId","emotions":["ANXIOUS"],"energy":"LOW","recordedDate":"${LocalDate.now()}"}"""
        }.andExpect { status { isOk() } }
        mvc.post("/api/v1/action-completions") {
            header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON
            content = """{"sessionId":"${UUID.randomUUID()}","moodId":"$moodId","actionId":"17E3A608-17F8-4BEA-94B3-370DFBF82D01"}"""
        }.andExpect { status { isOk() } }
        mvc.delete("/api/v1/me") { header("Authorization", "Bearer $token") }.andExpect { status { isOk() } }
        mvc.get("/api/v1/moods") { header("Authorization", "Bearer $token") }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `two simultaneous completions apply one reward`() {
        val token = token()
        val moodId = UUID.randomUUID()
        val sessionId = UUID.randomUUID()
        mvc.post("/api/v1/moods") {
            header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON
            content = """{"clientMoodId":"$moodId","emotions":["ANXIOUS"],"energy":"LOW","recordedDate":"${LocalDate.now()}"}"""
        }.andExpect { status { isOk() } }
        val body = """{"sessionId":"$sessionId","moodId":"$moodId","actionId":"17E3A608-17F8-4BEA-94B3-370DFBF82D01"}"""
        val start = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(2)
        try {
            val futures = (1..2).map {
                pool.submit<Int> {
                    start.await()
                    mvc.post("/api/v1/action-completions") { header("Authorization", "Bearer $token"); contentType = MediaType.APPLICATION_JSON; content = body }
                        .andReturn().response.status
                }
            }
            start.countDown()
            assertThat(futures.map { it.get(10, TimeUnit.SECONDS) }, everyItem(`is`(200)))
        } finally { pool.shutdownNow() }
        mvc.get("/api/v1/pets") { header("Authorization", "Bearer $token") }
            .andExpect { status { isOk() }; jsonPath("$[0].experience") { value(15) } }
    }

    @Test
    fun `action endpoint retains the stable catalog snapshot`() {
        mvc.get("/api/v1/actions").andExpect {
            status { isOk() }
            header { string("Cache-Control", "no-store") }
            header { string("X-Content-Type-Options", "nosniff") }
            jsonPath("$", hasSize<Any>(20))
            (1..20).forEachIndexed { index, number ->
                jsonPath("$[$index].id") { value("17E3A608-17F8-4BEA-94B3-370DFBF82D%02d".format(number)) }
            }
            jsonPath("$[0].instruction") { value("다른 일을 잠시 멈추고 음악 한 곡을 끝까지 들어보세요.") }
            jsonPath("$[0].symbolName") { value("headphones") }
            jsonPath("$[0].detailPrompt") { value("어떤 음악을 들었나요?") }
            jsonPath("$[0].supportedEmotions", hasItem("SAD"))
            jsonPath("$[19].catalogOrder") { value(19) }
        }
    }

    @Test
    fun `anonymous session limiter rejects requests above its configured window`() {
        val limiter = SessionRateLimiter(requests = 2, windowSeconds = 60)
        limiter.check("test-client")
        limiter.check("test-client")
        assertThrows<RateLimited> { limiter.check("test-client") }
    }
}
