package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.flatMap
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.LastRuleApplied
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import java.time.LocalDate
import javax.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ApplyCategoriesToTransactionsService(
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository,
  private val transactionRepository: TransactionRepository,
  private val lastRuleAppliedRepository: LastRuleAppliedRepository
) {
  companion object {
    private const val PAGE_SIZE = 25
  }

  private val log = LoggerFactory.getLogger(javaClass)

  @Transactional
  fun applyCategoriesToUnconfirmedTransactions(userId: TypedId<UserId>): TryEither<Unit> {
    log.debug("Starting to apply categories to all unconfirmed transactions for user $userId")
    val start = System.nanoTime()
    return applyCategoriesToUnconfirmedTransactions(userId, 0).map {
      val end = System.nanoTime()
      log.debug(
        "Finished applying categories to all unconfirmed transactions for user $userId. Time: ${end - start}")
    }
  }

  private fun applyCategoriesToUnconfirmedTransactions(
    userId: TypedId<UserId>,
    pageNumber: Int
  ): TryEither<Unit> {
    val page = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by(Sort.Order.asc("id")))
    return Either.catch { transactionRepository.findAllUnconfirmed(userId, page) }
      .flatMap { transactions ->
        applyCategoriesToTransactions(userId, transactions.content).map {
          transactions.totalElements
        }
      }
      .flatMap { totalElements ->
        if ((pageNumber + 1) * PAGE_SIZE < totalElements) {
          applyCategoriesToUnconfirmedTransactions(userId, pageNumber + 1)
        } else {
          Either.Right(Unit)
        }
      }
  }

  private fun deleteLastRuleAppliedForTransactions(
    userId: TypedId<UserId>,
    transactionIds: List<TypedId<TransactionId>>
  ): TryEither<Unit> =
    Either.catch {
      lastRuleAppliedRepository.deleteAllByUserIdAndTransactionIdIn(userId, transactionIds)
    }

  /**
   * This whole method is already O(n^2) complexity, so the read-only collection complexity doesn't
   * make it any worse.
   */
  private fun categorizeTransactionsReducer(
    fullContext: TransactionRuleContext,
    singleRuleContext: TransactionRuleContext
  ): TransactionRuleContext {
    val (_, _, rule) = singleRuleContext
    val (matches, noMatches) = fullContext.allTransactions.partition { doesRuleApply(it, rule) }
    val matchesWithCategories = matches.map { it.apply { categoryId = rule.categoryId } }
    val lastMatchingRules = matches.associate { it.uid to rule.uid }
    return fullContext.copy(
      allTransactions = matchesWithCategories + noMatches,
      lastRulesApplied = fullContext.lastRulesApplied + lastMatchingRules)
  }

  private fun saveCategorizedTransactions(
    userId: TypedId<UserId>,
    context: TransactionRuleContext
  ): TryEither<List<Transaction>> =
    Either.catch { transactionRepository.saveAllAndFlush(context.allTransactions) }
      .flatMap { transactions ->
        deleteLastRuleAppliedForTransactions(userId, transactions.map { it.uid }).map {
          transactions
        }
      }
      .flatMapCatch { transactions ->
        lastRuleAppliedRepository.saveAllAndFlush(
          context.lastRulesApplied.map {
            LastRuleApplied(userId = userId, transactionId = it.key, ruleId = it.value)
          })
        transactions
      }

  /** This returns the transactions in a different order from the method. */
  fun applyCategoriesToTransactions(
    userId: TypedId<UserId>,
    transactions: List<Transaction>
  ): TryEither<List<Transaction>> {
    log.debug("Starting to apply categories to batch of transactions for user $userId")
    val start = System.nanoTime()
    val categoryLessTransactions = transactions.map { it.apply { categoryId = null } }
    return Either.catch {
        autoCategorizeRuleRepository.streamAllByUserIdOrderByOrdinal(userId).use { ruleStream ->
          ruleStream
            .map { TransactionRuleContext.forCurrentRule(it) }
            .reduce(
              TransactionRuleContext.forAllTransactions(categoryLessTransactions),
              this::categorizeTransactionsReducer)
        }
      }
      .flatMap { saveCategorizedTransactions(userId, it) }
      .map { resultTransactions ->
        val end = System.nanoTime()
        log.debug(
          "Finished applying categories to batch of transactions for user $userId. Time: ${end - start}")
        resultTransactions
      }
  }

  private fun doesRuleApply(transaction: Transaction, rule: AutoCategorizeRule): Boolean =
    Regex(rule.regex).matches(transaction.description) &&
      (rule.startDate ?: LocalDate.MIN) <= transaction.expenseDate &&
      (rule.endDate ?: LocalDate.MAX) >= transaction.expenseDate &&
      (rule.minAmount?.let { it <= transaction.amount } ?: true) &&
      (rule.maxAmount?.let { it >= transaction.amount } ?: true)

  private data class TransactionRuleContext(
    val allTransactions: List<Transaction>,
    val lastRulesApplied: Map<TypedId<TransactionId>, TypedId<AutoCategorizeRuleId>>,
    val currentRule: AutoCategorizeRule
  ) {
    companion object {
      private val EMPTY_RULE = AutoCategorizeRule(0L, TypedId(), 0, "")

      fun forAllTransactions(allTransactions: List<Transaction>): TransactionRuleContext =
        TransactionRuleContext(allTransactions, mapOf(), EMPTY_RULE)
      fun forCurrentRule(currentRule: AutoCategorizeRule): TransactionRuleContext =
        TransactionRuleContext(listOf(), mapOf(), currentRule)
    }
  }
}
