package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import java.math.BigDecimal
import java.time.LocalDate

data class TransactionResponse(
  val id: TypedId<TransactionId>,
  val expenseDate: LocalDate,
  val description: String,
  val amount: BigDecimal,
  val confirmed: Boolean,
  val duplicate: Boolean,
  val categoryId: TypedId<CategoryId>?,
  val categoryName: String?
) {
  companion object {
    fun from(transaction: Transaction, category: Category? = null): TransactionResponse =
      TransactionResponse(
        id = transaction.id,
        expenseDate = transaction.expenseDate,
        description = transaction.description,
        amount = transaction.amount,
        confirmed = transaction.confirmed,
        duplicate = transaction.duplicate,
        categoryId = category?.id,
        categoryName = category?.name)
  }
}
