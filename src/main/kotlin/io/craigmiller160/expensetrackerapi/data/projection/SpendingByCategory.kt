package io.craigmiller160.expensetrackerapi.data.projection

import java.math.BigDecimal
import java.time.LocalDate

data class SpendingByCategory(
  val month: LocalDate,
  val categoryName: String,
  val amount: BigDecimal
)
