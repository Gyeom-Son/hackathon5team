package com.moodprint.backend.config

import com.moodprint.backend.api.ApiError
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

/** Reject oversized API writes before JSON parsing or controller allocation. */
@Component
class RequestSizeLimitFilter(
    @param:Value("\${moodprint.max-request-bytes:16384}") private val maxRequestBytes: Long,
) : OncePerRequestFilter() {
    private val objectMapper = ObjectMapper().findAndRegisterModules()
    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        !request.requestURI.startsWith("/api/") || request.method !in BODY_METHODS

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val body = request.inputStream.readNBytes(Math.toIntExact(maxRequestBytes + 1))
        if (body.size > maxRequestBytes) {
            response.status = HttpStatus.PAYLOAD_TOO_LARGE.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            objectMapper.writeValue(response.outputStream, ApiError("PAYLOAD_TOO_LARGE", "요청 크기는 ${maxRequestBytes}바이트를 넘을 수 없어요."))
            return
        }
        filterChain.doFilter(CachedBodyRequest(request, body), response)
    }

    private companion object {
        val BODY_METHODS = setOf("POST", "PUT", "PATCH")
    }
}

private class CachedBodyRequest(request: HttpServletRequest, private val body: ByteArray) :
    HttpServletRequestWrapper(request) {
    override fun getInputStream(): ServletInputStream {
        val input = ByteArrayInputStream(body)
        return object : ServletInputStream() {
            override fun read(): Int = input.read()
            override fun read(bytes: ByteArray, offset: Int, length: Int): Int = input.read(bytes, offset, length)
            override fun isFinished(): Boolean = input.available() == 0
            override fun isReady(): Boolean = true
            override fun setReadListener(listener: ReadListener?) {
                throw UnsupportedOperationException("Non-blocking reads are not supported")
            }
        }
    }
    override fun getReader(): BufferedReader = BufferedReader(InputStreamReader(inputStream, characterEncoding ?: Charsets.UTF_8.name()))
}
