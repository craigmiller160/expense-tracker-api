package io.craigmiller160.expensetrackerapi.data.projection

import com.querydsl.core.annotations.QueryProjection

data class NeedsAttentionCount
@QueryProjection
constructor(val type: NeedsAttentionType, val count: Long)
