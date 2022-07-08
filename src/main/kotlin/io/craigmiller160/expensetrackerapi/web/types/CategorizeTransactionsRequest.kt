package io.craigmiller160.expensetrackerapi.web.types

data class CategorizeTransactionsRequest(
    val transactionsAndCategories: Set<TransactionAndCategory>
)
