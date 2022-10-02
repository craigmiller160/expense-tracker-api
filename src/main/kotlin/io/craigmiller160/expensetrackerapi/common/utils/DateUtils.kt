package io.craigmiller160.expensetrackerapi.common.utils

import io.craigmiller160.expensetrackerapi.web.types.DATE_PATTERN
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
  const val DATE_PATTERN = "yyyy-MM-dd"
  private val DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN)

  fun format(date: LocalDate): String = DATE_FORMAT.format(date)
}
