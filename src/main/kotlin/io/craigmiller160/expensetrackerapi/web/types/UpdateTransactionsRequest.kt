package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId

data class TransactionToUpdate(
  override val transactionId: TypedId<TransactionId>,
  override val confirmed: Boolean,
  override val categoryId: TypedId<CategoryId>? = null
) : TransactionAndCategoryUpdateItem, TransactionAndConfirmUpdateItem

data class UpdateTransactionsRequest(val transactions: Set<TransactionToUpdate>)
