package io.craigmiller160.expensetrackerapi.data.projection

import java.time.LocalDate

data class NeedsAttentionOldest(val type: NeedsAttentionType, val date: LocalDate)
