package io.craigmiller160.expensetrackerapi.web

import io.craigmiller160.expensetrackerapi.common.error.BadRequestException
import io.craigmiller160.expensetrackerapi.web.types.ErrorResponse
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.transaction.InvalidTransactionException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
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

  @ExceptionHandler(BadRequestException::class)
  fun badRequestException(ex: BadRequestException): ResponseEntity<ErrorResponse> {
    log.error(ex.message, ex)
    return createErrorResponse(400, ex.message ?: "")
  }

  @ExceptionHandler(InvalidTransactionException::class)
  fun invalidTransactionException(ex: InvalidTransactionException): ResponseEntity<ErrorResponse> {
    log.error(ex.message, ex)
    return createErrorResponse(400, ex.message ?: "")
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun accessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
    log.error("Access Denied", ex)
    return createErrorResponse(403, "Access Denied")
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
