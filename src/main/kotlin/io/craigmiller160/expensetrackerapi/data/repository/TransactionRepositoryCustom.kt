package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.TransactionView
import io.craigmiller160.expensetrackerapi.web.types.transaction.SearchTransactionsRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TransactionRepositoryCustom {

  fun searchForTransactions(
    request: SearchTransactionsRequest,
    userId: TypedId<UserId>,
    page: Pageable
  ): Page<TransactionView>
}
