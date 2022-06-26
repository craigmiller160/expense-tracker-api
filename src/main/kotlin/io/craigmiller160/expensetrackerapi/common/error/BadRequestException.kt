package io.craigmiller160.expensetrackerapi.common.error

class BadRequestException(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)
