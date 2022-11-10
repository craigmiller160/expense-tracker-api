package io.craigmiller160.expensetrackerapi.web.types.report

import java.math.BigDecimal
import java.time.LocalDate

data class ReportMonthResponse(
  val date: LocalDate, // TODO can I make this a month instead?
  val total: BigDecimal
)
