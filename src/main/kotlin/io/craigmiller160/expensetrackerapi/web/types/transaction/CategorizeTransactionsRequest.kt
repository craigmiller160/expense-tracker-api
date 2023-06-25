package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.web.types.transaction.TransactionAndCategoryUpdateItem

data class TransactionAndCategory(
    override val transactionId: TypedId<TransactionId>,
    override val categoryId: TypedId<CategoryId>? = null
) : TransactionAndCategoryUpdateItem

// TODO needs validation
data class CategorizeTransactionsRequest(
    val transactionsAndCategories: Set<TransactionAndCategory>
)
