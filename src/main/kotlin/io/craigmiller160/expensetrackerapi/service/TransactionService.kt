package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.continuations.either
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionViewRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.transaction.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionService(
  private val transactionRepository: TransactionRepository,
  private val transactionViewRepository: TransactionViewRepository,
  private val lastRuleAppliedRepository: LastRuleAppliedRepository,
  private val categoryRepository: CategoryRepository
) {
  @Transactional
  fun categorizeTransactions(
    transactionsAndCategories: Set<TransactionAndCategoryUpdateItem>
  ): TryEither<Unit> {
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //    return transactionsAndCategories
    //      .map { txnAndCat ->
    //        Either.catch {
    //          txnAndCat.categoryId?.let {
    //            txnAndCat.transactionId to
    //              transactionRepository.setTransactionCategory(txnAndCat.transactionId, it,
    // userId)
    //          }
    //            ?: run {
    //              txnAndCat.transactionId to
    //                transactionRepository.removeTransactionCategory(txnAndCat.transactionId,
    // userId)
    //            }
    //        }
    //      }
    //      .sequence()
    //      .map(this::filterToModifiedTransactions)
    //      .flatMapCatch { transactionsWithModifiedCategory ->
    //        lastRuleAppliedRepository.deleteAllByUserIdAndTransactionIdIn(
    //          userId, transactionsWithModifiedCategory)
    //      }
    //      .map { Unit }
    TODO()
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
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //    return transactionsToConfirm
    //      .map { txnToConfirm ->
    //        Either.catch {
    //          txnToConfirm.transactionId to
    //            transactionRepository.confirmTransaction(
    //              txnToConfirm.transactionId, txnToConfirm.confirmed, userId)
    //        }
    //      }
    //      .sequence()
    //      .map(this::filterToModifiedTransactions)
    //      .flatMapCatch { transactionsWithModifiedCategory ->
    //        lastRuleAppliedRepository.deleteAllByUserIdAndTransactionIdIn(
    //          userId, transactionsWithModifiedCategory)
    //      }
    //      .map { Unit }
    TODO()
  }

  @Transactional
  fun deleteTransactions(request: DeleteTransactionsRequest): TryEither<Unit> {
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //    return Either.catch {
    //        lastRuleAppliedRepository.deleteAllByUserIdAndTransactionIdIn(userId, request.ids)
    //      }
    //      .flatMapCatch { transactionRepository.deleteTransactions(request.ids, userId) }
    TODO()
  }

  @Transactional
  fun search(request: SearchTransactionsRequest): TryEither<TransactionsPageResponse> {
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //
    //    val sort =
    //      Sort.by(
    //        Sort.Order(request.sortDirection.toSpringSortDirection(),
    // request.sortKey.toColumnName()),
    //        Sort.Order(Sort.Direction.ASC, "description"))
    //    val pageable = PageRequest.of(request.pageNumber, request.pageSize, sort)
    //    return getCategoryMap(userId)
    //      .map { categories -> request.categoryIds?.filter { categories.contains(it) }?.toSet() }
    //      .map { filteredCategories ->
    //        transactionRepository.searchForTransactions(
    //          request.copy(categoryIds = filteredCategories), userId, pageable)
    //      }
    //      .map { page -> TransactionsPageResponse.from(page) }
    TODO()
  }

  @Transactional
  fun createTransaction(request: CreateTransactionRequest): TryEither<TransactionResponse> {
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //
    //    return Either.catch {
    //      val validCategory =
    //        request.categoryId?.let { categoryRepository.findByUidAndUserId(it, userId) }
    //      val transaction =
    //        Transaction(
    //          userId = userId,
    //          expenseDate = request.expenseDate,
    //          description = request.description,
    //          amount = request.amount,
    //          confirmed = true,
    //          categoryId = validCategory?.uid)
    //      val dbTransaction = transactionRepository.saveAndFlush(transaction)
    //      transactionViewRepository
    //        .findById(dbTransaction.uid)
    //        .map { TransactionResponse.from(it) }
    //        .orElseThrow {
    //          IllegalStateException("Cannot find created transaction in database:
    // ${dbTransaction.uid}")
    //        }
    //    }
    TODO()
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
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //
    //    return either
    //      .eager {
    //        val oldTransaction =
    //          Either.catch { transactionRepository.findByUidAndUserId(transactionId, userId) }
    //            .leftIfNull { BadRequestException("No transaction with ID: $transactionId") }
    //            .bind()
    //        val validCategoryId =
    //          Either.catch {
    //              request.categoryId?.let { categoryRepository.findByUidAndUserId(it, userId)
    // }?.uid
    //            }
    //            .bind()
    //        val oldValues =
    //          OldConfirmedAndCategory(
    //            confirmed = oldTransaction.confirmed, categoryId = oldTransaction.categoryId)
    //        oldValues to
    //          oldTransaction.apply {
    //            confirmed = request.confirmed
    //            expenseDate = request.expenseDate
    //            description = request.description
    //            amount = request.amount
    //            categoryId = validCategoryId
    //          }
    //      }
    //      .flatMapCatch { (oldValues, newTransaction) ->
    //        oldValues to transactionRepository.save(newTransaction)
    //      }
    //      .flatMapCatch { (oldValues, newTransaction) ->
    //        if (oldValues.categoryId != newTransaction.categoryId ||
    //          oldValues.confirmed != newTransaction.confirmed) {
    //          lastRuleAppliedRepository.deleteAllByUserIdAndTransactionIdIn(
    //            userId, listOf(newTransaction.uid))
    //        }
    //      }
    //      .map { Unit }
    TODO()
  }

  private data class OldConfirmedAndCategory(
    val confirmed: Boolean,
    val categoryId: TypedId<CategoryId>?
  )

  fun getPossibleDuplicates(
    transactionId: TypedId<TransactionId>,
    request: GetPossibleDuplicatesRequest
  ): TryEither<TransactionDuplicatePageResponse> {
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //    val pageable = PageRequest.of(request.pageNumber, request.pageSize)
    //    return Either.catch {
    //      val pageResult = transactionViewRepository.findAllDuplicates(transactionId, userId,
    // pageable)
    //      TransactionDuplicatePageResponse.from(pageResult)
    //    }
    TODO()
  }

  @Transactional
  fun markNotDuplicate(transactionId: TypedId<TransactionId>): TryEither<Unit> {
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //    return Either.catch {
    //      transactionRepository.markNotDuplicate(System.nanoTime(), transactionId, userId)
    //    }
    TODO()
  }

  private fun getCategoryMap(userId: Long): TryEither<Map<TypedId<CategoryId>, Category>> =
    Either.catch { categoryRepository.findAllByUserIdOrderByName(userId).associateBy { it.uid } }

  fun getTransactionDetails(
    transactionId: TypedId<TransactionId>
  ): TryEither<TransactionDetailsResponse> {
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //    return Either.catch { transactionViewRepository.findByUidAndUserId(transactionId, userId)
    // }
    //      .flatMap { txn ->
    //        txn?.let { Either.Right(it) }
    //          ?: Either.Left(BadRequestException("No transaction for ID: $transactionId"))
    //      }
    //      .map { TransactionDetailsResponse.from(it) }
    TODO()
  }
}
