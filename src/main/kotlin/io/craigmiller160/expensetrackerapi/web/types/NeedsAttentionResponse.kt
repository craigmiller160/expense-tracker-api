package io.craigmiller160.expensetrackerapi.web.types

import java.time.LocalDate

data class CountAndOldest(val count: Long, val oldest: LocalDate?)

data class NeedsAttentionResponse(
  val unconfirmed: CountAndOldest,
  val uncategorized: CountAndOldest,
  val duplicate: CountAndOldest
)
