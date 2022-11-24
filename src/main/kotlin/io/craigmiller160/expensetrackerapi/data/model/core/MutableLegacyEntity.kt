package io.craigmiller160.expensetrackerapi.data.model.core

import java.time.ZonedDateTime
import javax.persistence.PreUpdate

interface MutableLegacyEntity<T> : LegacyEntity<T> {
  var updated: ZonedDateTime
  val version: Long

  @PreUpdate
  fun onPreUpdate() {
    updated = ZonedDateTime.now()
  }
}
