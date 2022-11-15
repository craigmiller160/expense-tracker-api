package io.craigmiller160.expensetrackerapi.web.types.rules

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import java.time.LocalDate

data class AutoCategorizeRuleResponse(
  val id: TypedId<AutoCategorizeRuleId>,
  val categoryId: TypedId<CategoryId>,
  val ordinal: Int,
  val regex: String,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val minAmount: LocalDate? = null,
  val maxAmount: LocalDate? = null
)
