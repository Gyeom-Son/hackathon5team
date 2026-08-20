package com.moodprint.backend.api

import com.moodprint.backend.config.CurrentUser
import com.moodprint.backend.config.SessionRateLimiter
import com.moodprint.backend.domain.ActionCatalog
import com.moodprint.backend.domain.AnonymousUser
import com.moodprint.backend.domain.PetCatalog
import com.moodprint.backend.repository.PetRepository
import com.moodprint.backend.service.*
import jakarta.validation.Valid
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController @RequestMapping("/api/v1/anonymous-sessions")
class SessionController(private val service: SessionService, private val limiter: SessionRateLimiter) {
    @PostMapping fun create(request: HttpServletRequest): AnonymousSessionResponse {
        limiter.check(request.remoteAddr ?: "unknown")
        return service.create()
    }
}

@RestController @RequestMapping("/api/v1/moods")
class MoodController(private val service: MoodService) {
    @PostMapping fun create(@CurrentUser user: AnonymousUser, @Valid @RequestBody request: CreateMoodRequest) = service.create(user, request)
    @GetMapping fun all(
        @CurrentUser user: AnonymousUser,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "100") @Min(1) @Max(500) size: Int,
    ) = service.limited(user, page, size)
}

@RestController @RequestMapping("/api/v1/me")
class AccountController(private val service: AccountService) {
    @DeleteMapping fun delete(@CurrentUser user: AnonymousUser) = service.delete(user)
}

@RestController @RequestMapping("/api/v1/actions")
class ActionController {
    @GetMapping fun all() = ActionCatalog.actions.map { it.response() }
}

@RestController @RequestMapping("/api/v1/recommendations")
class RecommendationController(private val service: RecommendationService) {
    @PostMapping fun recommend(@CurrentUser user: AnonymousUser, @Valid @RequestBody request: RecommendationRequest) = service.recommend(user, request)
}

@RestController @RequestMapping("/api/v1/action-completions")
class CompletionController(private val service: CompletionService) {
    @PostMapping fun complete(@CurrentUser user: AnonymousUser, @Valid @RequestBody request: CompleteActionRequest) = service.complete(user, request)
}

@RestController @RequestMapping("/api/v1/pets")
class PetController(private val pets: PetRepository) {
    @GetMapping fun all(@CurrentUser user: AnonymousUser) = PetCatalog.sorted(pets.findAllByOwnerId(user.id)).map { it.response() }
}

@Validated @RestController @RequestMapping("/api/v1/records")
class RecordsController(private val moods: MoodService) {
    @GetMapping("/monthly")
    fun monthly(@CurrentUser user: AnonymousUser, @RequestParam @Min(2000) @Max(2100) year: Int, @RequestParam @Min(1) @Max(12) month: Int) = moods.monthly(user, year, month)
}
