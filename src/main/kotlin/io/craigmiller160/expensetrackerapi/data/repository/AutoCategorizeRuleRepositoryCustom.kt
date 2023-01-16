package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId

interface AutoCategorizeRuleRepositoryCustom {

  fun decrementOrdinals(
    userId: TypedId<UserId>,
    minOrdinal: Int,
    maxOrdinal: Int,
    excludeId: TypedId<AutoCategorizeRuleId>? = null
  )

  fun incrementOrdinals(
    userId: TypedId<UserId>,
    minOrdinal: Int,
    maxOrdinal: Int,
    excludeId: TypedId<AutoCategorizeRuleId>? = null
  )
}
