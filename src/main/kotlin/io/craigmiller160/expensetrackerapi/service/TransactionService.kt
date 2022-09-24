package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.flatMap
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.toColumnName
import io.craigmiller160.expensetrackerapi.data.model.toSpringSortDirection
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.web.types.CountAndOldest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.NeedsAttentionResponse
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsResponse
import io.craigmiller160.expensetrackerapi.web.types.TransactionAndCategoryUpdateItem
import io.craigmiller160.expensetrackerapi.web.types.TransactionAndConfirmUpdateItem
import io.craigmiller160.expensetrackerapi.web.types.UpdateTransactionsRequest
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionService(
  private val transactionRepository: TransactionRepository,
  private val categoryRepository: CategoryRepository,
  private val oAuth2Service: OAuth2Service
) {
  @Transactional
  fun categorizeTransactions(
    transactionsAndCategories: Set<TransactionAndCategoryUpdateItem>
  ): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return transactionsAndCategories.toList().foldRight<
      TransactionAndCategoryUpdateItem, TryEither<Unit>>(
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
  fun confirmTransactions(
    transactionsToConfirm: Set<TransactionAndConfirmUpdateItem>
  ): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return transactionsToConfirm.toList().foldRight<
      TransactionAndConfirmUpdateItem, TryEither<Unit>>(
      Either.Right(Unit)) { txn, result ->
        result.flatMapCatch {
          transactionRepository.confirmTransaction(txn.transactionId, txn.confirmed, userId)
        }
      }
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
        Sort.Order(request.sortDirection.toSpringSortDirection(), request.sortKey.toColumnName()),
        Sort.Order(Sort.Direction.ASC, "description"))
    val pageable = PageRequest.of(request.pageNumber, request.pageSize, sort)
    val categoryMapEither = getCategoryMap(userId)
    return categoryMapEither
      .map { categories -> request.categoryIds?.filter { categories.contains(it) }?.toSet() }
      .map { filteredCategories ->
        transactionRepository.searchForTransactions3(
          request.copy(categoryIds = filteredCategories), userId, pageable)
      }
      .flatMap { page -> categoryMapEither.map { Pair(page, it) } }
      .map { (page, categories) -> SearchTransactionsResponse.from(page, categories) }
  }

  @Transactional
  fun updateTransactions(request: UpdateTransactionsRequest): TryEither<Unit> =
    either.eager {
      categorizeTransactions(request.transactions).bind()
      confirmTransactions(request.transactions).bind()
    }

  private fun getCategoryMap(userId: Long): TryEither<Map<TypedId<CategoryId>, Category>> =
    Either.catch { categoryRepository.findAllByUserIdOrderByName(userId).associateBy { it.id } }
}
