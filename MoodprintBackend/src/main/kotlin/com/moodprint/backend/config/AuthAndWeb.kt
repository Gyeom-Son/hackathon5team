package com.moodprint.backend.config

import com.moodprint.backend.domain.AnonymousUser
import com.moodprint.backend.repository.AnonymousUserRepository
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import java.time.Clock
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import com.moodprint.backend.service.RateLimited
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.security.MessageDigest
import java.time.ZoneId
import java.util.HexFormat

@Target(AnnotationTarget.VALUE_PARAMETER) @Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUser

@Component
class CurrentUserResolver(private val users: AnonymousUserRepository) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.hasParameterAnnotation(CurrentUser::class.java) && parameter.parameterType == AnonymousUser::class.java
    override fun resolveArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer?, webRequest: NativeWebRequest, binderFactory: WebDataBinderFactory?): Any {
        val header = webRequest.getHeader(HttpHeaders.AUTHORIZATION) ?: throw UnauthorizedException()
        val token = header.takeIf { it.startsWith("Bearer ") }?.removePrefix("Bearer ")?.trim().orEmpty()
        if (token.length < 32) throw UnauthorizedException()
        return users.findByTokenHash(hashToken(token)) ?: throw UnauthorizedException()
    }
}

fun hashToken(token: String): String = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.toByteArray()))
class UnauthorizedException : RuntimeException("유효한 익명 토큰이 필요해요.")

@Component
class SessionRateLimiter(
    @param:Value("\${moodprint.session-rate-limit.requests:20}") private val requests: Int,
    @param:Value("\${moodprint.session-rate-limit.window-seconds:60}") private val windowSeconds: Long,
) {
    private val clock: Clock = Clock.systemUTC()
    private val attempts = ConcurrentHashMap<String, ArrayDeque<Long>>()

    fun check(key: String) {
        val now = clock.millis()
        val cutoff = now - windowSeconds * 1_000
        val bucket = attempts.computeIfAbsent(key) { ArrayDeque() }
        synchronized(bucket) {
            while (bucket.firstOrNull()?.let { it <= cutoff } == true) bucket.removeFirst()
            if (bucket.size >= requests) throw RateLimited("잠시 후 다시 시도해 주세요.")
            bucket.addLast(now)
        }
        if (attempts.size > 10_000) attempts.entries.removeIf { (_, value) -> synchronized(value) { value.lastOrNull()?.let { it <= cutoff } ?: true } }
    }
}

@Component
class RequestCorrelationFilter : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val supplied = request.getHeader("X-Request-ID")?.takeIf { it.length in 1..64 && it.all { char -> char.isLetterOrDigit() || char in "-_." } }
        val requestId = supplied ?: UUID.randomUUID().toString()
        MDC.put("requestId", requestId)
        response.setHeader("X-Request-ID", requestId)
        response.setHeader("X-Content-Type-Options", "nosniff")
        if (request.requestURI.startsWith("/api/")) response.setHeader("Cache-Control", "no-store")
        try { filterChain.doFilter(request, response) } finally { MDC.remove("requestId") }
    }
}

@Configuration
class WebConfig(
    private val resolver: CurrentUserResolver,
    @param:Value("\${moodprint.cors.allowed-origins:}") private val allowedOrigins: String,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) { resolvers += resolver }
    override fun addCorsMappings(registry: CorsRegistry) {
        val origins = allowedOrigins.split(',').map(String::trim).filter(String::isNotEmpty)
        if (origins.isNotEmpty()) {
            registry.addMapping("/api/**")
                .allowedOrigins(*origins.toTypedArray())
                .allowedMethods("GET", "POST", "DELETE")
        }
    }
}

@Configuration
class TimeConfig(
    @param:Value("\${moodprint.time-zone:Asia/Seoul}") private val timeZone: String,
) {
    @Bean fun moodprintClock(): Clock = Clock.system(ZoneId.of(timeZone))
}
