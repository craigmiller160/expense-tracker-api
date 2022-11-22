package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import java.math.BigDecimal
import java.time.LocalDate

data class AutoCategorizeRuleView(
  val id: TypedId<AutoCategorizeRuleId>,
  val userId: Long,
  val ordinal: Int,
  val regex: String,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val minAmount: BigDecimal? = null,
  val maxAmount: BigDecimal? = null
)
