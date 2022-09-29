package io.craigmiller160.expensetrackerapi.data.projection

import java.time.LocalDate
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class NeedsAttentionOldest(
  @Enumerated(EnumType.STRING) val type: NeedsAttentionType,
  val date: LocalDate
)
