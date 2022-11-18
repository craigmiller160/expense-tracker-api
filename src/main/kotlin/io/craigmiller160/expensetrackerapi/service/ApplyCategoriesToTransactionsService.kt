package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.flatMap
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
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
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ApplyCategoriesToTransactionsService(
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository,
  private val transactionRepository: TransactionRepository,
  private val lastRuleAppliedRepository: LastRuleAppliedRepository
) {
  // TODO major refactor here is needed to clean this up
  companion object {
    private const val PAGE_SIZE = 25
  }

  @Transactional
  fun applyCategoriesToUnconfirmedTransactions(userId: Long): TryEither<Unit> =
    applyCategoriesToUnconfirmedTransactions(userId, 0)

  private fun applyCategoriesToUnconfirmedTransactions(
    userId: Long,
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

  fun applyCategoriesToTransactions(
    userId: Long,
    transactions: List<Transaction>
  ): TryEither<List<Transaction>> {
    val categoryLessTransactions = transactions.map { it.copy(categoryId = null) }
    lastRuleAppliedRepository.deleteAllByUserIdAndTransactionIdIn(
      userId, categoryLessTransactions.map { it.id })
    return Either.catch {
        autoCategorizeRuleRepository.streamAllByUserIdOrderByOrdinal(userId).use { ruleStream ->
          ruleStream
            .map { TemporaryRuleTransactionsWrapper.fromRule(it) }
            .reduce(TemporaryRuleTransactionsWrapper.fromTransactions(categoryLessTransactions)) {
              (_, txnsToCategorize),
              (rule) ->
              TemporaryRuleTransactionsWrapper.fromTransactionAndRules(
                txnsToCategorize.map { applyRule(it, rule) })
            }
            .transactions
        }
      }
      .flatMapCatch { transactionsAndRules ->
        val lastRuleApplied = transactionsAndRules.mapNotNull { it.toLastRuleApplied(userId) }
        val transactionsToSave = transactionsAndRules.map { it.transaction }
        lastRuleAppliedRepository.saveAll(lastRuleApplied)
        transactionRepository.saveAll(transactionsToSave)
      }
  }

  private fun applyRule(
    transactionAndRule: TransactionAndRule,
    rule: AutoCategorizeRule
  ): TransactionAndRule =
    if (doesRuleApply(transactionAndRule.transaction, rule))
      TransactionAndRule(
        transaction = transactionAndRule.transaction.copy(categoryId = rule.categoryId),
        rule = rule)
    else transactionAndRule

  private fun doesRuleApply(transaction: Transaction, rule: AutoCategorizeRule): Boolean =
    Regex(rule.regex).matches(transaction.description) &&
      (rule.startDate ?: LocalDate.MIN) <= transaction.expenseDate &&
      (rule.endDate ?: LocalDate.MAX) >= transaction.expenseDate &&
      (rule.minAmount?.let { it <= transaction.amount } ?: true) &&
      (rule.maxAmount?.let { it >= transaction.amount } ?: true)

  private data class TransactionAndRule(
    val transaction: Transaction,
    val rule: AutoCategorizeRule? = null
  ) {
    fun toLastRuleApplied(userId: Long): LastRuleApplied? =
      rule?.let { LastRuleApplied(userId = userId, ruleId = it.id, transactionId = transaction.id) }
  }

  // TODO heavily refactor this and its use
  private data class TemporaryRuleTransactionsWrapper(
    val rule: AutoCategorizeRule,
    val transactions: List<TransactionAndRule>
  ) {
    companion object {
      private val EMPTY_RULE = AutoCategorizeRule(0L, TypedId(), 0, "")
      fun fromRule(rule: AutoCategorizeRule): TemporaryRuleTransactionsWrapper =
        TemporaryRuleTransactionsWrapper(rule, listOf())
      fun fromTransactions(transactions: List<Transaction>): TemporaryRuleTransactionsWrapper =
        TemporaryRuleTransactionsWrapper(EMPTY_RULE, transactions.map { TransactionAndRule(it) })

      fun fromTransactionAndRules(
        transactionAndRules: List<TransactionAndRule>
      ): TemporaryRuleTransactionsWrapper =
        TemporaryRuleTransactionsWrapper(EMPTY_RULE, transactionAndRules)
    }
  }
}
