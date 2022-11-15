package io.craigmiller160.expensetrackerapi.web.types.rules

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import java.math.BigDecimal
import java.time.LocalDate

data class AutoCategorizeRuleResponse(
  val id: TypedId<AutoCategorizeRuleId>,
  val categoryId: TypedId<CategoryId>,
  val ordinal: Int,
  val regex: String,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val minAmount: BigDecimal? = null,
  val maxAmount: BigDecimal? = null
) {
  companion object {
    fun from(rule: AutoCategorizeRule): AutoCategorizeRuleResponse =
      AutoCategorizeRuleResponse(
        id = rule.id,
        categoryId = rule.categoryId,
        ordinal = rule.ordinal,
        regex = rule.regex,
        startDate = rule.startDate,
        endDate = rule.endDate,
        minAmount = rule.minAmount,
        maxAmount = rule.maxAmount)
  }
}
