package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.flatMap
import arrow.core.leftIfNull
import arrow.core.sequence
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.error.BadRequestException
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.model.toColumnName
import io.craigmiller160.expensetrackerapi.data.model.toSpringSortDirection
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionViewRepository
import io.craigmiller160.expensetrackerapi.extension.detachAndReturn
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.web.types.*
import io.craigmiller160.expensetrackerapi.web.types.transaction.*
import io.craigmiller160.oauth2.service.OAuth2Service
import javax.persistence.EntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionService(
  private val transactionRepository: TransactionRepository,
  private val transactionViewRepository: TransactionViewRepository,
  private val lastRuleAppliedRepository: LastRuleAppliedRepository,
  private val categoryRepository: CategoryRepository,
  private val oAuth2Service: OAuth2Service,
  private val entityManager: EntityManager
) {
  @Transactional
  fun categorizeTransactions(
    transactionsAndCategories: Set<TransactionAndCategoryUpdateItem>
  ): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return transactionsAndCategories
      .map { txnAndCat ->
        Either.catch {
          txnAndCat.categoryId?.let {
            txnAndCat.transactionId to
              transactionRepository.setTransactionCategory(txnAndCat.transactionId, it, userId)
          }
            ?: run {
              txnAndCat.transactionId to
                transactionRepository.removeTransactionCategory(txnAndCat.transactionId, userId)
            }
        }
      }
      .sequence()
      .map(this::filterToModifiedTransactions)
      .flatMapCatch { transactionsWithModifiedCategory ->
        lastRuleAppliedRepository.deleteAllByUserIdAndTransactionIdIn(
          userId, transactionsWithModifiedCategory)
      }
      .map { Unit }
  }

  private fun filterToModifiedTransactions(
    results: List<Pair<TypedId<TransactionId>, Int>>
  ): List<TypedId<TransactionId>> =
    results
      .filter { (_, modifiedCount) -> modifiedCount > 0 }
      .map { (transactionId) -> transactionId }

  @Transactional
  fun confirmTransactions(
    transactionsToConfirm: Set<TransactionAndConfirmUpdateItem>
  ): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return transactionsToConfirm
      .map { txnToConfirm ->
        Either.catch {
          txnToConfirm.transactionId to
            transactionRepository.confirmTransaction(
              txnToConfirm.transactionId, txnToConfirm.confirmed, userId)
        }
      }
      .sequence()
      .map(this::filterToModifiedTransactions)
      .flatMapCatch { transactionsWithModifiedCategory ->
        lastRuleAppliedRepository.deleteAllByUserIdAndTransactionIdIn(
          userId, transactionsWithModifiedCategory)
      }
      .map { Unit }
  }

  @Transactional
  fun deleteTransactions(request: DeleteTransactionsRequest): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch {
        lastRuleAppliedRepository.deleteAllByUserIdAndTransactionIdIn(userId, request.ids)
      }
      .flatMapCatch { transactionRepository.deleteTransactions(request.ids, userId) }
  }

  @Transactional
  fun search(request: SearchTransactionsRequest): TryEither<TransactionsPageResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId

    val sort =
      Sort.by(
        Sort.Order(request.sortDirection.toSpringSortDirection(), request.sortKey.toColumnName()),
        Sort.Order(Sort.Direction.ASC, "description"))
    val pageable = PageRequest.of(request.pageNumber, request.pageSize, sort)
    return getCategoryMap(userId)
      .map { categories -> request.categoryIds?.filter { categories.contains(it) }?.toSet() }
      .map { filteredCategories ->
        transactionRepository.searchForTransactions(
          request.copy(categoryIds = filteredCategories), userId, pageable)
      }
      .map { page -> TransactionsPageResponse.from(page) }
  }

  @Transactional
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
      val dbTransaction = transactionRepository.saveAndFlush(transaction)
      transactionViewRepository
        .findById(dbTransaction.id)
        .map { TransactionResponse.from(it) }
        .orElseThrow {
          IllegalStateException("Cannot find created transaction in database: ${dbTransaction.id}")
        }
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

    return either
      .eager {
        val oldTransaction =
          Either.catch { transactionRepository.findByIdAndUserId(transactionId, userId) }
            .leftIfNull { BadRequestException("No transaction with ID: $transactionId") }
            .flatMapCatch { entityManager.detachAndReturn(it) }
            .bind()
        val validCategoryId =
          Either.catch {
              request.categoryId?.let { categoryRepository.findByIdAndUserId(it, userId) }?.id
            }
            .bind()
        val oldValues =
          OldConfirmedAndCategory(
            confirmed = oldTransaction.confirmed, categoryId = oldTransaction.categoryId)
        oldValues to
          oldTransaction.apply {
            confirmed = request.confirmed
            expenseDate = request.expenseDate
            description = request.description
            amount = request.amount
            categoryId = validCategoryId
          }
      }
      .flatMapCatch { (oldValues, newTransaction) ->
        oldValues to transactionRepository.save(newTransaction)
      }
      .flatMapCatch { (oldValues, newTransaction) ->
        if (oldValues.categoryId != newTransaction.categoryId ||
          oldValues.confirmed != newTransaction.confirmed) {
          lastRuleAppliedRepository.deleteAllByUserIdAndTransactionIdIn(
            userId, listOf(newTransaction.id))
        }
      }
      .map { Unit }
  }

  private data class OldConfirmedAndCategory(
    val confirmed: Boolean,
    val categoryId: TypedId<CategoryId>?
  )

  fun getPossibleDuplicates(
    transactionId: TypedId<TransactionId>,
    request: GetPossibleDuplicatesRequest
  ): TryEither<TransactionDuplicatePageResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    val pageable = PageRequest.of(request.pageNumber, request.pageSize)
    return Either.catch {
      val pageResult = transactionViewRepository.findAllDuplicates(transactionId, userId, pageable)
      TransactionDuplicatePageResponse.from(pageResult)
    }
  }

  @Transactional
  fun markNotDuplicate(transactionId: TypedId<TransactionId>): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch {
      transactionRepository.markNotDuplicate(System.nanoTime(), transactionId, userId)
    }
  }

  private fun getCategoryMap(userId: Long): TryEither<Map<TypedId<CategoryId>, Category>> =
    Either.catch { categoryRepository.findAllByUserIdOrderByName(userId).associateBy { it.id } }

  fun getTransactionDetails(
    transactionId: TypedId<TransactionId>
  ): TryEither<TransactionDetailsResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch { transactionViewRepository.findByIdAndUserId(transactionId, userId) }
      .flatMap { txn ->
        txn?.let { Either.Right(it) }
          ?: Either.Left(BadRequestException("No transaction for ID: $transactionId"))
      }
      .map { TransactionDetailsResponse.from(it) }
  }
}
