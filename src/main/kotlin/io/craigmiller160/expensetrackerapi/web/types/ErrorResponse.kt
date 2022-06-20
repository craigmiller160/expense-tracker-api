package io.craigmiller160.expensetrackerapi.web.types

import java.time.ZonedDateTime

data class ErrorResponse(
    val timestamp: ZonedDateTime,
    val method: String,
    val path: String,
    val status: Int,
    val message: String
)
