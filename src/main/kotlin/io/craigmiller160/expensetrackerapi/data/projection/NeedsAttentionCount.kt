package io.craigmiller160.expensetrackerapi.data.projection

import javax.persistence.EnumType
import javax.persistence.Enumerated

data class NeedsAttentionCount(
  @Enumerated(EnumType.STRING) val type: NeedsAttentionType,
  val count: Int
)
