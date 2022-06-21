package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.continuations.either
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.web.types.CategorizeTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsResponse
import io.craigmiller160.expensetrackerapi.web.types.TransactionAndCategory
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val oAuth2Service: OAuth2Service
) {
  @Transactional
  fun categorizeTransactions(request: CategorizeTransactionsRequest): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return request.transactionsAndCategories.foldRight<TransactionAndCategory, TryEither<Unit>>(
        Either.Right(Unit)) { txnAndCat, result ->
      result.flatMapCatch {
        transactionRepository.setTransactionCategory(
            txnAndCat.transactionId, txnAndCat.categoryId, userId)
      }
    }
  }

  @Transactional
  fun deleteTransactions(request: DeleteTransactionsRequest): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch { transactionRepository.deleteTransactions(request.ids, userId) }
  }

  @Transactional
  fun search(request: SearchTransactionsRequest): TryEither<SearchTransactionsResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    val pageable = PageRequest.of(request.pageNumber, request.pageSize)
    return either.eager {
      val categories = getCategoryMap(userId).bind()
      val page =
          Either.catch {
                transactionRepository.searchTransactions(
                    userId,
                    request.startDate,
                    request.endDate,
                    request.confirmed,
                    request.categoryIds,
                    pageable)
              }
              .bind()

      SearchTransactionsResponse.from(page, categories)
    }
  }

  private fun getCategoryMap(userId: Long): TryEither<Map<TypedId<CategoryId>, Category>> =
      Either.catch { categoryRepository.findAllByUserId(userId).associateBy { it.id } }
}
