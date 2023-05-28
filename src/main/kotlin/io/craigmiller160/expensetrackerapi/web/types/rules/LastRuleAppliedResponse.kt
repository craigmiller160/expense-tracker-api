package io.craigmiller160.expensetrackerapi.web.types.rules

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.LastRuleAppliedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.projection.LastRuleAppliedForTransaction
import java.math.BigDecimal
import java.time.LocalDate

data class LastRuleAppliedResponse(
    val id: TypedId<LastRuleAppliedId>,
    val ruleId: TypedId<AutoCategorizeRuleId>,
    val transactionId: TypedId<TransactionId>,
    val categoryId: TypedId<CategoryId>,
    val categoryName: String,
    val ordinal: Int,
    val regex: String,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null
) {
  companion object {
    fun from(lastRule: LastRuleAppliedForTransaction): LastRuleAppliedResponse =
        LastRuleAppliedResponse(
            id = lastRule.uid,
            ruleId = lastRule.ruleId,
            transactionId = lastRule.transactionId,
            categoryId = lastRule.categoryId,
            categoryName = lastRule.categoryName,
            ordinal = lastRule.ordinal,
            regex = lastRule.regex,
            startDate = lastRule.startDate,
            endDate = lastRule.endDate,
            minAmount = lastRule.minAmount,
            maxAmount = lastRule.maxAmount)
  }
}
