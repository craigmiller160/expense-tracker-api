package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TransactionRepositoryCustom {
  // TODO rename this
  fun searchForTransactions2(
    request: SearchTransactionsRequest,
    categories: List<TypedId<CategoryId>>?,
    page: Pageable
  ): Page<Transaction>

  fun searchForTransactions3(
    request: SearchTransactionsRequest,
    userId: Long,
    page: Pageable
  ): Page<Transaction>
}
