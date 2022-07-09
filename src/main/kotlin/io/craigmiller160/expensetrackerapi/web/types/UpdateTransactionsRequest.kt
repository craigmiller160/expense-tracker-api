package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId

data class TransactionToUpdate(
    val transactionId: TypedId<TransactionId>,
    val categoryId: TypedId<CategoryId>?,
    val confirmed: Boolean
)

data class UpdateTransactionsRequest(val transactions: Set<TransactionToUpdate>)
