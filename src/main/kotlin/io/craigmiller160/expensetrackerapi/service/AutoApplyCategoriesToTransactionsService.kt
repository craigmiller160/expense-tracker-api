package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.oauth2.service.OAuth2Service
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
        .map { rule -> transactions to rule }
        .reduce { (transactions, _), (_, rule) ->
          // TODO what about the first element, need to apply the first rule to it
          transactions to rule
        }
    }
    TODO()
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
