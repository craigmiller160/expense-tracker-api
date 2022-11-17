package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.flatMap
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import java.math.BigDecimal
import java.time.LocalDate
import javax.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ApplyCategoriesToTransactionsService(
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository,
  private val transactionRepository: TransactionRepository
) {
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
        applyCategoriesToTransactions(userId, transactions.content)
          .flatMapCatch { transactionRepository.saveAll(it) }
          .map { transactions.totalElements }
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
  ): TryEither<List<Transaction>> =
    Either.catch {
      autoCategorizeRuleRepository.streamAllByUserIdOrderByOrdinal(userId).use { ruleStream ->
        ruleStream
          .map { RuleTransactionsWrapper.fromRule(it) }
          .reduce(RuleTransactionsWrapper.fromTransactions(transactions)) {
            (_, transactions),
            (rule) ->
            RuleTransactionsWrapper.fromTransactions(transactions.map { applyRule(it, rule) })
          }
          .transactions
      }
    }

  private fun applyRule(transaction: Transaction, rule: AutoCategorizeRule): Transaction =
    if (doesRuleApply(transaction, rule)) transaction.copy(categoryId = rule.categoryId)
    else transaction

  private fun doesRuleApply(transaction: Transaction, rule: AutoCategorizeRule): Boolean =
    Regex(rule.regex).matches(transaction.description) &&
      (rule.startDate ?: LocalDate.MIN) <= transaction.expenseDate &&
      (rule.endDate ?: LocalDate.MAX) >= transaction.expenseDate &&
      (rule.minAmount ?: BigDecimal(Double.MIN_VALUE)) <= transaction.amount &&
      (rule.maxAmount ?: BigDecimal(Double.MAX_VALUE)) >= transaction.amount

  private data class RuleTransactionsWrapper(
    val rule: AutoCategorizeRule,
    val transactions: List<Transaction>
  ) {
    companion object {
      private val EMPTY_RULE = AutoCategorizeRule(0L, TypedId(), 0, "")
      fun fromRule(rule: AutoCategorizeRule): RuleTransactionsWrapper =
        RuleTransactionsWrapper(rule, listOf())
      fun fromTransactions(transactions: List<Transaction>): RuleTransactionsWrapper =
        RuleTransactionsWrapper(EMPTY_RULE, transactions)
    }
  }
}
