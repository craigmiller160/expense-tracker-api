package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionCount
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionOldest

interface NeedsAttentionRepository {
  fun getAllNeedsAttentionCounts(userId: TypedId<UserId>): List<NeedsAttentionCount>

  fun getAllNeedsAttentionOldest(userId: TypedId<UserId>): List<NeedsAttentionOldest>
}
