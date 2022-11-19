package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.flatMap
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
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
  // TODO look into persitent collections option for this
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
    return Either.catch {
        lastRuleAppliedRepository.deleteAllByUserIdAndTransactionIdIn(
          userId, categoryLessTransactions.map { it.id })
      }
      .flatMapCatch {
        autoCategorizeRuleRepository.streamAllByUserIdOrderByOrdinal(userId).use { ruleStream ->
          ruleStream
            .map { TransactionRuleContext.forCurrentRule(it) }
            .reduce(TransactionRuleContext.forAllTransactions(categoryLessTransactions)) {
              context,
              (_, _, rule) ->
              val (matches, noMatches) =
                context.allTransactions.partition { doesRuleApply(it, rule) }
              val matchesWithCategories = matches.map { it.copy(categoryId = rule.categoryId) }
              val lastMatchingRules = matches.associate { it.id to rule.id }
              context.copy(
                allTransactions = matchesWithCategories + noMatches,
                lastRulesApplied = context.lastRulesApplied + lastMatchingRules)
            }

          //          ruleStream
          //            .map { TemporaryRuleTransactionsWrapper.fromRule(it) }
          //
          // .reduce(TemporaryRuleTransactionsWrapper.fromTransactions(categoryLessTransactions)) {
          //              (_, txnsToCategorize),
          //              (rule) ->
          //              TemporaryRuleTransactionsWrapper.fromTransactionAndRules(
          //                txnsToCategorize.map { applyRule(it, rule) })
          //            }
          //            .transactions
        }
      }
      .flatMapCatch { context ->
        //        val lastRuleApplied = transactionsAndRules.mapNotNull {
        // it.toLastRuleApplied(userId) }
        //        val transactionsToSave = transactionsAndRules.map { it.transaction }
        lastRuleAppliedRepository.saveAll(
          context.lastRulesApplied.map {
            LastRuleApplied(userId = userId, transactionId = it.key, ruleId = it.value)
          })
        transactionRepository.saveAll(context.allTransactions)
      }
  }

  private fun applyRule(
    transactionAndRule: TransactionAndRule2,
    rule: AutoCategorizeRule
  ): TransactionAndRule2 =
    if (doesRuleApply(transactionAndRule.transaction, rule))
      TransactionAndRule2(
        transaction = transactionAndRule.transaction.copy(categoryId = rule.categoryId),
        rule = rule)
    else transactionAndRule

  private fun doesRuleApply(transaction: Transaction, rule: AutoCategorizeRule): Boolean =
    Regex(rule.regex).matches(transaction.description) &&
      (rule.startDate ?: LocalDate.MIN) <= transaction.expenseDate &&
      (rule.endDate ?: LocalDate.MAX) >= transaction.expenseDate &&
      (rule.minAmount?.let { it <= transaction.amount } ?: true) &&
      (rule.maxAmount?.let { it >= transaction.amount } ?: true)

  private data class TransactionAndRule2(
    val transaction: Transaction,
    val rule: AutoCategorizeRule? = null
  ) {
    fun toLastRuleApplied(userId: Long): LastRuleApplied? =
      rule?.let { LastRuleApplied(userId = userId, ruleId = it.id, transactionId = transaction.id) }
  }

  // TODO heavily refactor this and its use
  private data class TemporaryRuleTransactionsWrapper(
    val rule: AutoCategorizeRule,
    val transactions: List<TransactionAndRule2>
  ) {
    companion object {
      private val EMPTY_RULE = AutoCategorizeRule(0L, TypedId(), 0, "")
      fun fromRule(rule: AutoCategorizeRule): TemporaryRuleTransactionsWrapper =
        TemporaryRuleTransactionsWrapper(rule, listOf())
      fun fromTransactions(transactions: List<Transaction>): TemporaryRuleTransactionsWrapper =
        TemporaryRuleTransactionsWrapper(EMPTY_RULE, transactions.map { TransactionAndRule2(it) })

      fun fromTransactionAndRules(
        transactionAndRules: List<TransactionAndRule2>
      ): TemporaryRuleTransactionsWrapper =
        TemporaryRuleTransactionsWrapper(EMPTY_RULE, transactionAndRules)
    }
  }

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
