package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import org.springframework.data.domain.Page

interface AutoCategorizeRuleRepositoryCustom {
  fun searchForRules(request: AutoCategorizeRulePageRequest, userId: Long): Page<AutoCategorizeRule>

  fun decrementOrdinals(
    userId: Long,
    minOrdinal: Int,
    maxOrdinal: Int,
    excludeId: TypedId<AutoCategorizeRuleId>? = null
  )

  fun incrementOrdinals(
    userId: Long,
    minOrdinal: Int,
    maxOrdinal: Int,
    excludeId: TypedId<AutoCategorizeRuleId>? = null
  )
}
