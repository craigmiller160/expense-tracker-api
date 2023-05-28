package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.rules.LastRuleAppliedResponse
import org.springframework.stereotype.Service

@Service
class LastRuleAppliedService(
    private val lastRuleAppliedRepository: LastRuleAppliedRepository,
    private val authService: AuthorizationService
) {
  fun getLastAppliedRuleForTransaction(
      transactionId: TypedId<TransactionId>
  ): TryEither<LastRuleAppliedResponse?> {
    val userId = authService.getAuthUserId()
    return Either.catch {
          lastRuleAppliedRepository.getLastRuleDetailsForTransaction(userId, transactionId)
        }
        .map { result -> result?.let { LastRuleAppliedResponse.from(it) } }
  }
}
