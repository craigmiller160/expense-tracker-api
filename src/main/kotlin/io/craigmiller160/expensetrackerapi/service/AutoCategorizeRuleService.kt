package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageResponse
import org.springframework.stereotype.Service

@Service
class AutoCategorizeRuleService(
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository
) {
  fun getAllRules(
    request: AutoCategorizeRulePageRequest
  ): TryEither<AutoCategorizeRulePageResponse> = TODO()
}
