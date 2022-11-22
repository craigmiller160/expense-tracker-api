package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRuleView
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleViewRepositoryCustom
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import org.springframework.data.domain.Page
import org.springframework.stereotype.Repository

@Repository
class AutoCategorizeRuleViewRepositoryCustomImpl : AutoCategorizeRuleViewRepositoryCustom {
  override fun searchForRules(
    request: AutoCategorizeRulePageRequest,
    userId: Long
  ): Page<AutoCategorizeRuleView> {
    TODO("Not yet implemented")
  }
}
