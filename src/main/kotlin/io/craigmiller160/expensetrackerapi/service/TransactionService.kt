package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.flatMap
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.data.specification.SpecBuilder
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.web.types.CategorizeTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsResponse
import io.craigmiller160.expensetrackerapi.web.types.TransactionAndCategory
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
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
    val categoryMapEither = getCategoryMap(userId)
    return categoryMapEither
        .map { categories -> createSearchSpec(userId, request, categories.keys) }
        .flatMapCatch { spec -> transactionRepository.findAll(spec, pageable) }
        .flatMap { page -> categoryMapEither.map { Pair(page, it) } }
        .map { (page, categories) -> SearchTransactionsResponse.from(page, categories) }
  }

  private fun createSearchSpec(
      userId: Long,
      request: SearchTransactionsRequest,
      categories: Set<TypedId<CategoryId>>
  ): Specification<Transaction> {
    val userIdSpec = SpecBuilder.equals<Transaction>(userId, "userId")
    val startDateSpec =
        SpecBuilder.greaterThanOrEqualTo<Transaction>(request.startDate, "expenseDate")
    val endDateSpec = SpecBuilder.lessThanOrEqualTo<Transaction>(request.endDate, "expenseDate")
    val confirmedSpec = SpecBuilder.equals<Transaction>(request.confirmed, "confirmed")
    val filteredCategoryIds =
        request.categoryIds?.let { ids -> ids.filter { categories.contains(it) } }
    val categoryIdSpec = SpecBuilder.`in`<Transaction>(filteredCategoryIds, "categoryId")

    return userIdSpec.and(startDateSpec).and(endDateSpec).and(confirmedSpec).and(categoryIdSpec)
  }

  private fun getCategoryMap(userId: Long): TryEither<Map<TypedId<CategoryId>, Category>> =
      Either.catch { categoryRepository.findAllByUserId(userId).associateBy { it.id } }
}
