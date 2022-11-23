package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId

interface AutoCategorizeRuleRepositoryCustom {

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
