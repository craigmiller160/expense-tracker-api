package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.oauth2.service.OAuth2Service
import java.math.BigDecimal
import java.time.LocalDate
import org.springframework.stereotype.Service

@Service
class AutoApplyCategoriesToTransactionsService(
  private val oAuth2Service: OAuth2Service,
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository
) {
  fun applyCategoriesToTransactions(transactions: List<Transaction>): TryEither<List<Transaction>> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    // TODO wrap in Either
    autoCategorizeRuleRepository.streamAllByUserIdOrderByOrdinal(userId).use { ruleStream ->
      // TODO do a map reduce
      ruleStream
        .map { RuleTransactionsWrapper.fromRule(it) }
        .reduce(RuleTransactionsWrapper.fromTransactions(transactions)) { (_, transactions), (rule)
          ->
          RuleTransactionsWrapper.fromTransactions(transactions)
        }
    }
    TODO()
  }

  private fun doesRuleApply(transaction: Transaction, rule: AutoCategorizeRule): Boolean {
    return Regex(rule.regex).matches(transaction.description) &&
      (rule.startDate ?: LocalDate.MIN) <= transaction.expenseDate &&
      (rule.endDate ?: LocalDate.MAX) >= transaction.expenseDate &&
      (rule.minAmount ?: BigDecimal(Double.MIN_VALUE)) <= transaction.amount &&
      (rule.maxAmount ?: BigDecimal(Double.MAX_VALUE)) >= transaction.amount
  }

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
