package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.model.TransactionView
import org.springframework.data.domain.Page

data class TransactionsPageResponse(
  val transactions: List<TransactionResponse>,
  override val pageNumber: Int,
  override val totalItems: Long
) : PageableResponse {
  companion object {
    fun from(
      page: Page<Transaction>,
      categories: Map<TypedId<CategoryId>, Category>
    ): TransactionsPageResponse {
      val transactions =
        page.content.map { txn ->
          val category = txn.categoryId?.let { categories[it] }
          TransactionResponse.from(txn, category)
        }
      return TransactionsPageResponse(
        transactions = transactions, pageNumber = page.number, totalItems = page.totalElements)
    }

    fun from(page: Page<TransactionView>): TransactionsPageResponse =
      TransactionsPageResponse(
        transactions = page.content.map { TransactionResponse.from(it) },
        pageNumber = page.number,
        totalItems = page.totalElements)
  }
}
