package io.craigmiller160.expensetrackerapi.data.projection

import java.math.BigDecimal
import java.time.LocalDate

data class SpendingByMonth(val month: LocalDate, val total: BigDecimal)
