package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionCount
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionOldest

interface NeedsAttentionRepository {
  fun getAllNeedsAttentionCounts(userId: Long): List<NeedsAttentionCount>

  fun getAllNeedsAttentionOldest(userId: Long): List<NeedsAttentionOldest>
}
