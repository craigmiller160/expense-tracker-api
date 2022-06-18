package io.craigmiller160.expensetrackerapi.web

import io.craigmiller160.expensetrackerapi.web.types.ErrorResponse
import java.time.ZoneId
import java.time.ZonedDateTime
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@ControllerAdvice
class ControllerErrorHandler {
  private val log = LoggerFactory.getLogger(ControllerErrorHandler::class.java)
  @ExceptionHandler(Exception::class)
  fun exception(ex: Exception): ResponseEntity<ErrorResponse> {
    log.error(ex.message, ex)
    return createErrorResponse(500, ex.message ?: "")
  }

  private fun createErrorResponse(status: Int, message: String): ResponseEntity<ErrorResponse> {
    val (method, uri) = getMethodAndUri()
    val response =
        ErrorResponse(
            timestamp = ZonedDateTime.now(ZoneId.of("UTC")),
            method = method,
            path = uri,
            message = message,
            status = status)
    return ResponseEntity.status(status).body(response)
  }

  private fun getMethodAndUri(): Pair<String, String> =
      RequestContextHolder.getRequestAttributes()
          ?.let {
            when (it) {
              is ServletRequestAttributes -> it
              else -> null
            }
          }
          ?.request?.let { request -> Pair(request.method, request.requestURI) }
          ?: Pair("", "")
}
