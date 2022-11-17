package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import org.springframework.stereotype.Service

@Service
class AutoApplyCategoriesToTransactionsService(
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository
) {
  fun applyCategoriesToTransactions(transactions: List<Transaction>): TryEither<List<Transaction>> =
    TODO()
}
