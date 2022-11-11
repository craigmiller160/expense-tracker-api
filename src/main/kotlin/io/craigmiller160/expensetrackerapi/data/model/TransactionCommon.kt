package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import java.math.BigDecimal
import java.time.LocalDate

interface TransactionCommon {
  val id: TypedId<TransactionId>
  val userId: Long
  val expenseDate: LocalDate
  val description: String
  val amount: BigDecimal
  val confirmed: Boolean
  val categoryId: TypedId<CategoryId>?
}
