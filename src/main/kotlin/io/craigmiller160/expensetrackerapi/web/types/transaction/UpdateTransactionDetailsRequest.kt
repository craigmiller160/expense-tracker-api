package io.craigmiller160.expensetrackerapi.web.types.transaction

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import java.math.BigDecimal
import java.time.LocalDate

data class UpdateTransactionDetailsRequest(
  override val transactionId: TypedId<TransactionId>,
  override val confirmed: Boolean,
  val expenseDate: LocalDate,
  val description: String,
  val amount: BigDecimal,
  override val categoryId: TypedId<CategoryId>?
) : TransactionAndCategoryUpdateItem, TransactionAndConfirmUpdateItem
