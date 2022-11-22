package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRuleView
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import org.springframework.data.domain.Page

interface AutoCategorizeRuleViewRepositoryCustom {
  fun searchForRules(
    request: AutoCategorizeRulePageRequest,
    userId: Long
  ): Page<AutoCategorizeRuleView>
}
