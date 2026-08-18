package com.moodprint.backend.api

import com.moodprint.backend.config.UnauthorizedException
import com.moodprint.backend.service.Conflict
import com.moodprint.backend.service.NotFound
import com.moodprint.backend.service.RateLimited
import jakarta.validation.ConstraintViolationException
import org.springframework.http.*
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.*
import java.time.Instant
import org.slf4j.LoggerFactory
import org.slf4j.MDC

data class ApiError(
    val code: String,
    val message: String,
    val timestamp: Instant = Instant.now(),
    val requestId: String? = MDC.get("requestId"),
)

@RestControllerAdvice
class ApiErrorHandler {
    private val logger = LoggerFactory.getLogger(ApiErrorHandler::class.java)
    @ExceptionHandler(UnauthorizedException::class) @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun unauthorized(e: UnauthorizedException) = ApiError("UNAUTHORIZED", e.message!!)
    @ExceptionHandler(NotFound::class, NoSuchElementException::class) @ResponseStatus(HttpStatus.NOT_FOUND)
    fun notFound(e: RuntimeException) = ApiError("NOT_FOUND", e.message ?: "찾을 수 없어요.")
    @ExceptionHandler(Conflict::class) @ResponseStatus(HttpStatus.CONFLICT)
    fun conflict(e: Conflict) = ApiError("CONFLICT", e.message!!)
    @ExceptionHandler(RateLimited::class)
    fun rateLimited(e: RateLimited): ResponseEntity<ApiError> = ResponseEntity
        .status(HttpStatus.TOO_MANY_REQUESTS)
        .header(HttpHeaders.RETRY_AFTER, "60")
        .body(ApiError("RATE_LIMITED", e.message ?: "잠시 후 다시 시도해 주세요."))
    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        ConstraintViolationException::class,
        IllegalArgumentException::class,
        HttpMessageNotReadableException::class,
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class,
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalid(e: Exception): ApiError {
        val message = when (e) {
            is MethodArgumentNotValidException -> e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            is HttpMessageNotReadableException -> "요청 JSON 형식을 확인해 주세요."
            is MethodArgumentTypeMismatchException, is MissingServletRequestParameterException -> "요청 파라미터를 확인해 주세요."
            is IllegalArgumentException -> e.message
            else -> "입력값을 확인해 주세요."
        }
        return ApiError("INVALID_REQUEST", message ?: "입력값을 확인해 주세요.")
    }
    @ExceptionHandler(Exception::class) @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun unexpected(e: Exception): ApiError {
        // Request bodies and user notes are deliberately not included in logs.
        logger.error("Unhandled API error requestId={}", MDC.get("requestId"), e)
        return ApiError("INTERNAL_ERROR", "요청을 처리하지 못했어요.")
    }
}
