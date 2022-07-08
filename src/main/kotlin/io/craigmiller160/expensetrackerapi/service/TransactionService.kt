package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.continuations.either
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
import io.craigmiller160.expensetrackerapi.web.types.ConfirmTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.CountAndOldest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.NeedsAttentionResponse
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsResponse
import io.craigmiller160.expensetrackerapi.web.types.TransactionAndCategory
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
  fun confirmTransactions(request: ConfirmTransactionsRequest): TryEither<Unit> {
    TODO()
  }

  @Transactional
  fun deleteTransactions(request: DeleteTransactionsRequest): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch { transactionRepository.deleteTransactions(request.ids, userId) }
  }

  @Transactional
  fun getNeedsAttention(): TryEither<NeedsAttentionResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return either.eager {
      val unconfirmedCount =
          Either.catch { transactionRepository.countAllUnconfirmed(userId) }.bind()
      val oldestUnconfirmed =
          Either.catch { transactionRepository.getOldestUnconfirmedDate(userId) }.bind()
      val uncategorizedCount =
          Either.catch { transactionRepository.countAllUncategorized(userId) }.bind()
      val oldestUncategorized =
          Either.catch { transactionRepository.getOldestUncategorizedDate(userId) }.bind()
      val duplicateCount = Either.catch { transactionRepository.countAllDuplicates(userId) }.bind()
      val oldestDuplicate = Either.catch { transactionRepository.getOldestDuplicate(userId) }.bind()
      NeedsAttentionResponse(
          unconfirmed = CountAndOldest(count = unconfirmedCount, oldest = oldestUnconfirmed),
          uncategorized = CountAndOldest(count = uncategorizedCount, oldest = oldestUncategorized),
          duplicate = CountAndOldest(count = duplicateCount, oldest = oldestDuplicate))
    }
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
    val confirmedSpec = SpecBuilder.equals<Transaction>(request.isConfirmed, "confirmed")
    val duplicateSpec = SpecBuilder.equals<Transaction>(request.isDuplicate, "duplicate")
    val filteredCategoryIds =
        request.categoryIds?.let { ids -> ids.filter { categories.contains(it) } }
    val categoryIdSpec = SpecBuilder.`in`<Transaction>(filteredCategoryIds, "categoryId")
    val isCategorizedSpec =
        when (request.isCategorized) {
          null -> SpecBuilder.emptySpec()
          true -> SpecBuilder.isNotNull<Transaction>("categoryId")
          false -> SpecBuilder.isNull<Transaction>("categoryId")
        }

    return userIdSpec
        .and(startDateSpec)
        .and(endDateSpec)
        .and(confirmedSpec)
        .and(categoryIdSpec)
        .and(isCategorizedSpec)
        .and(duplicateSpec)
  }

  private fun getCategoryMap(userId: Long): TryEither<Map<TypedId<CategoryId>, Category>> =
      Either.catch { categoryRepository.findAllByUserIdOrderByName(userId).associateBy { it.id } }
}
