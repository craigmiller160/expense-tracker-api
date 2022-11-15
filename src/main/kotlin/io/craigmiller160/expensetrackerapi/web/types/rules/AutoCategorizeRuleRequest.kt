package io.craigmiller160.expensetrackerapi.web.types.rules

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import java.time.LocalDate

// TODO when creating, the ordinal is the last one in list
// TODO when updating, the ordinal is the existing one
data class AutoCategorizeRuleRequest(
  val categoryId: TypedId<CategoryId>,
  val regex: String,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val minAmount: LocalDate? = null,
  val maxAmount: LocalDate? = null
)
