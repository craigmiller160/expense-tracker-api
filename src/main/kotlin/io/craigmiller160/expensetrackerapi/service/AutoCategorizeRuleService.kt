package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageResponse
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleResponse
import io.craigmiller160.oauth2.service.OAuth2Service
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AutoCategorizeRuleService(
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository,
  private val oAuth2Service: OAuth2Service
) {
  fun getAllRules(
    request: AutoCategorizeRulePageRequest
  ): TryEither<AutoCategorizeRulePageResponse> = TODO()

  @Transactional
  fun createRule(request: AutoCategorizeRuleRequest): TryEither<AutoCategorizeRuleResponse> = TODO()

  @Transactional
  fun updateRule(
    ruleId: TypedId<AutoCategorizeRuleId>,
    request: AutoCategorizeRuleRequest
  ): TryEither<AutoCategorizeRuleResponse> = TODO()

  fun getRule(ruleId: TypedId<AutoCategorizeRuleId>): TryEither<AutoCategorizeRuleResponse> = TODO()

  @Transactional fun deleteRule(ruleId: TypedId<AutoCategorizeRuleId>): TryEither<Unit> = TODO()

  @Transactional
  fun reOrderRule(ruleId: TypedId<AutoCategorizeRuleId>, ordinal: Int): TryEither<Unit> = TODO()
}
