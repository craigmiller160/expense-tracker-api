package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepositoryCustom
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import org.springframework.data.domain.Page
import org.springframework.stereotype.Repository

@Repository
class AutoCategorizeRuleRepositoryCustomImpl : AutoCategorizeRuleRepositoryCustom {
  override fun searchForRules(
    request: AutoCategorizeRulePageRequest,
    userId: Long
  ): Page<AutoCategorizeRule> {
    TODO("Not yet implemented")
  }
}
