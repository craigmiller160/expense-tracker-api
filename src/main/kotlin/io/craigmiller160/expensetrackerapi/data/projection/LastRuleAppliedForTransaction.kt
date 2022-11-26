package io.craigmiller160.expensetrackerapi.data.projection

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.LastRuleAppliedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import java.math.BigDecimal
import java.time.LocalDate

data class LastRuleAppliedForTransaction(
  val uid: TypedId<LastRuleAppliedId>,
  val ruleId: TypedId<AutoCategorizeRuleId>,
  val transactionId: TypedId<TransactionId>,
  val userId: Long,
  val categoryId: TypedId<CategoryId>,
  val categoryName: String,
  val ordinal: Int,
  val regex: String,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val minAmount: BigDecimal? = null,
  val maxAmount: BigDecimal? = null
)
