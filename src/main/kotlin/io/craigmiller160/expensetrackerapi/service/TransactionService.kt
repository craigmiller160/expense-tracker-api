package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.CategorizeTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.TransactionResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
  @Transactional
  fun categorizeTransactions(request: CategorizeTransactionsRequest): TryEither<Unit> {
    TODO()
  }

  @Transactional
  fun deleteTransactions(request: DeleteTransactionsRequest): TryEither<Unit> {
    TODO()
  }

  fun search(request: SearchTransactionsRequest): TryEither<List<TransactionResponse>> {
    TODO()
  }
}
