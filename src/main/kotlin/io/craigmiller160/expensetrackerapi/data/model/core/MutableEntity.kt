package io.craigmiller160.expensetrackerapi.data.model.core

import java.time.ZonedDateTime
import javax.persistence.PreUpdate

interface MutableEntity<T> : Entity<T> {
  var updated: ZonedDateTime
  val version: Long

  @PreUpdate
  fun onPreUpdate() {
    updated = ZonedDateTime.now()
  }
}
