package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import org.springframework.data.domain.Page

data class SearchTransactionsResponse(
    val transactions: List<TransactionResponse>,
    val pageNumber: Int,
    val totalItems: Long
) {
  companion object {
    fun from(
        page: Page<Transaction>,
        categories: Map<TypedId<CategoryId>, Category>
    ): SearchTransactionsResponse {
      val transactions =
          page.content.map { txn ->
            val category = txn.categoryId?.let { categories[it] }
            TransactionResponse.from(txn, category)
          }
      return SearchTransactionsResponse(
          transactions = transactions, pageNumber = page.number, totalItems = page.totalElements)
    }
  }
}
