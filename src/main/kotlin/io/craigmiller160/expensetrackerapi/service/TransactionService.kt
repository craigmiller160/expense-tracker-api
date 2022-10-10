package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.flatMap
import arrow.core.flatten
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.error.BadRequestException
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.model.toColumnName
import io.craigmiller160.expensetrackerapi.data.model.toSpringSortDirection
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionType
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.web.types.CountAndOldest
import io.craigmiller160.expensetrackerapi.web.types.CreateTransactionRequest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.NeedsAttentionResponse
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsResponse
import io.craigmiller160.expensetrackerapi.web.types.TransactionAndCategoryUpdateItem
import io.craigmiller160.expensetrackerapi.web.types.TransactionAndConfirmUpdateItem
import io.craigmiller160.expensetrackerapi.web.types.TransactionResponse
import io.craigmiller160.expensetrackerapi.web.types.UpdateTransactionDetailsRequest
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
    return Either.catch {
      val needsAttentionCounts =
        transactionRepository.getAllNeedsAttentionCounts(userId).associateBy { it.type }
      val needsAttentionOldest =
        transactionRepository.getAllNeedsAttentionOldest(userId).associateBy { it.type }

      NeedsAttentionResponse(
        unconfirmed =
          CountAndOldest(
            count = needsAttentionCounts[NeedsAttentionType.UNCONFIRMED]!!.count,
            oldest = needsAttentionOldest[NeedsAttentionType.UNCONFIRMED]?.date),
        uncategorized =
          CountAndOldest(
            count = needsAttentionCounts[NeedsAttentionType.UNCATEGORIZED]!!.count,
            oldest = needsAttentionOldest[NeedsAttentionType.UNCATEGORIZED]?.date),
        duplicate =
          CountAndOldest(
            count = needsAttentionCounts[NeedsAttentionType.DUPLICATE]!!.count,
            oldest = needsAttentionOldest[NeedsAttentionType.DUPLICATE]?.date),
        possibleRefund =
          CountAndOldest(
            count = needsAttentionCounts[NeedsAttentionType.POSSIBLE_REFUND]!!.count,
            oldest = needsAttentionOldest[NeedsAttentionType.POSSIBLE_REFUND]?.date))
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
        transactionRepository.searchForTransactions(
          request.copy(categoryIds = filteredCategories), userId, pageable)
      }
      .flatMap { page -> categoryMapEither.map { Pair(page, it) } }
      .map { (page, categories) -> SearchTransactionsResponse.from(page, categories) }
  }

  fun createTransaction(request: CreateTransactionRequest): TryEither<TransactionResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId

    return Either.catch {
      val validCategory =
        request.categoryId?.let { categoryRepository.findByIdAndUserId(it, userId) }
      val transaction =
        Transaction(
          userId = userId,
          expenseDate = request.expenseDate,
          description = request.description,
          amount = request.amount,
          confirmed = true,
          categoryId = validCategory?.id)
      transactionRepository.save(transaction).let { TransactionResponse.from(it, validCategory) }
    }
  }

  @Transactional
  fun updateTransactions(request: UpdateTransactionsRequest): TryEither<Unit> =
    either.eager {
      categorizeTransactions(request.transactions).bind()
      confirmTransactions(request.transactions).bind()
    }

  @Transactional
  fun updateTransactionDetails(
    transactionId: TypedId<TransactionId>,
    request: UpdateTransactionDetailsRequest
  ): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId

    return Either.catch {
        val validCategoryId =
          request.categoryId?.let { categoryRepository.findByIdAndUserId(it, userId) }?.id
        transactionRepository
          .findByIdAndUserId(transactionId, userId)
          ?.copy(
            confirmed = request.confirmed,
            expenseDate = request.expenseDate,
            description = request.description,
            amount = request.amount,
            categoryId = validCategoryId)
          ?.let { transactionRepository.save(it) }
          ?.let { Either.Right(Unit) }
          ?: Either.Left(BadRequestException("No transaction with ID: $transactionId"))
      }
      .flatten()
  }

  private fun getCategoryMap(userId: Long): TryEither<Map<TypedId<CategoryId>, Category>> =
    Either.catch { categoryRepository.findAllByUserIdOrderByName(userId).associateBy { it.id } }
}
