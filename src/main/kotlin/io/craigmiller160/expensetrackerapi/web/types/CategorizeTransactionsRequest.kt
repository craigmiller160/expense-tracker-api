package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId

data class TransactionAndCategory(
    override val transactionId: TypedId<TransactionId>,
    override val categoryId: TypedId<CategoryId>? = null
) : TransactionAndCategoryUpdateItem

data class CategorizeTransactionsRequest(
    val transactionsAndCategories: Set<TransactionAndCategory>
)
