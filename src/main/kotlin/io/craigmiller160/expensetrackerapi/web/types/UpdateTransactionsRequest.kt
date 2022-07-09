package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId

data class TransactionToUpdate(
    override val transactionId: TypedId<TransactionId>,
    override val categoryId: TypedId<CategoryId>?,
    override val confirmed: Boolean
) : TransactionAndCategoryUpdateItem, TransactionAndConfirmUpdateItem

data class UpdateTransactionsRequest(val transactions: Set<TransactionToUpdate>)
