package io.craigmiller160.expensetrackerapi.web.types.transaction

import io.craigmiller160.expensetrackerapi.data.model.TransactionView
import io.craigmiller160.expensetrackerapi.web.types.PageableResponse
import org.springframework.data.domain.Page

data class TransactionDuplicatePageResponse(
    val transactions: List<TransactionDuplicateResponse>,
    override val pageNumber: Int,
    override val totalItems: Long
) : PageableResponse {
  companion object {
    fun from(transactions: Page<TransactionView>): TransactionDuplicatePageResponse =
        TransactionDuplicatePageResponse(
            transactions = transactions.content.map { TransactionDuplicateResponse.from(it) },
            pageNumber = transactions.number,
            totalItems = transactions.totalElements)
  }
}
