package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionCount
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TransactionRepositoryCustom {

  fun searchForTransactions(
    request: SearchTransactionsRequest,
    userId: Long,
    page: Pageable
  ): Page<Transaction>

  fun getAllNeedsAttentionCounts(userId: Long): List<NeedsAttentionCount>
}
