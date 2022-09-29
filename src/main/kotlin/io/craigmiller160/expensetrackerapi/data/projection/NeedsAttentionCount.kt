package io.craigmiller160.expensetrackerapi.data.projection

data class NeedsAttentionCount(val type: NeedsAttentionType, val count: Long) {
  constructor(type: String, count: Long) : this(NeedsAttentionType.valueOf(type), count)
}
