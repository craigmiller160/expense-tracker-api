package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.rules.LastRuleAppliedResponse
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.stereotype.Service

@Service
class LastRuleAppliedService(
  private val lastRuleAppliedRepository: LastRuleAppliedRepository,
  private val oAuth2Service: OAuth2Service
) {
  fun getLastAppliedRuleForTransaction(
    transactionId: TypedId<TransactionId>
  ): TryEither<LastRuleAppliedResponse?> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch {
        lastRuleAppliedRepository.getLastRuleDetailsForTransaction(userId, transactionId)
      }
      .map { result -> result?.let { LastRuleAppliedResponse.from(it) } }
  }
}
