package io.craigmiller160.expensetrackerapi.web.types

data class SearchTransactionsResponse(
    val transactions: List<TransactionResponse>,
    val pageNumber: Int,
    val totalItems: Long
)
