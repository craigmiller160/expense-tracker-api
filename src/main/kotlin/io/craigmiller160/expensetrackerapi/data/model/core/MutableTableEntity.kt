package io.craigmiller160.expensetrackerapi.data.model.core

import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version
import java.time.ZonedDateTime

@MappedSuperclass
abstract class MutableTableEntity<T> : TableEntity<T>() {
  var updated: ZonedDateTime = ZonedDateTime.now()
  @Version var version: Long = 1

  override fun onPrePersist() {
    super.onPrePersist()
    updated = ZonedDateTime.now()
  }

  override fun onPreUpdate() {
    super.onPreUpdate()
    updated = ZonedDateTime.now()
  }
}
