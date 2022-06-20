package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.sequence
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.CategorizeTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.TransactionResponse
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val oAuth2Service: OAuth2Service
) {
  @Transactional
  fun categorizeTransactions(request: CategorizeTransactionsRequest): TryEither<List<Unit>> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return request.transactionsAndCategories
        .map { txnAndCat ->
          Either.catch {
            transactionRepository.setTransactionCategory(
                txnAndCat.transactionId, txnAndCat.categoryId, userId)
          }
        }
        .sequence()
  }

  @Transactional
  fun deleteTransactions(request: DeleteTransactionsRequest): TryEither<Unit> {
    TODO()
  }

  fun search(request: SearchTransactionsRequest): TryEither<List<TransactionResponse>> {
    TODO()
  }
}
