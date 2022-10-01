package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import java.time.LocalDate

data class CreateTransactionRequest(
  val expenseDate: LocalDate,
  val description: String,
  val amount: Double,
  val categoryId: TypedId<CategoryId>?
)
