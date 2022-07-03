package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.flatMap
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.model.toColumnName
import io.craigmiller160.expensetrackerapi.data.model.toSpringSortDirection
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
import io.craigmiller160.expensetrackerapi.web.types.TransactionCategoryType
import io.craigmiller160.expensetrackerapi.web.types.UnconfirmedTransactionCountResponse
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
        txnAndCat.categoryId?.let {
          transactionRepository.setTransactionCategory(txnAndCat.transactionId, it, userId)
        }
            ?: transactionRepository.removeTransactionCategory(txnAndCat.transactionId, userId)
      }
    }
  }

  @Transactional
  fun deleteTransactions(request: DeleteTransactionsRequest): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch { transactionRepository.deleteTransactions(request.ids, userId) }
  }

  fun getUnconfirmedCount(): TryEither<UnconfirmedTransactionCountResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch { transactionRepository.countAllByUserIdAndConfirmed(userId, false) }
        .map { UnconfirmedTransactionCountResponse(it) }
  }

  @Transactional
  fun search(request: SearchTransactionsRequest): TryEither<SearchTransactionsResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId

    val sort =
        Sort.by(
            Sort.Order(
                request.sortDirection.toSpringSortDirection(), request.sortKey.toColumnName()),
            Sort.Order(Sort.Direction.ASC, "description"))
    val pageable = PageRequest.of(request.pageNumber, request.pageSize, sort)
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
    val categoryTypeSpec =
        when (request.categoryType) {
          null,
          TransactionCategoryType.ALL -> SpecBuilder.emptySpec()
          TransactionCategoryType.WITH_CATEGORY -> SpecBuilder.isNotNull<Transaction>("categoryId")
          TransactionCategoryType.WITHOUT_CATEGORY -> SpecBuilder.isNull<Transaction>("categoryId")
        }

    return userIdSpec
        .and(startDateSpec)
        .and(endDateSpec)
        .and(confirmedSpec)
        .and(categoryIdSpec)
        .and(categoryTypeSpec)
  }

  private fun getCategoryMap(userId: Long): TryEither<Map<TypedId<CategoryId>, Category>> =
      Either.catch { categoryRepository.findAllByUserIdOrderByName(userId).associateBy { it.id } }
}
