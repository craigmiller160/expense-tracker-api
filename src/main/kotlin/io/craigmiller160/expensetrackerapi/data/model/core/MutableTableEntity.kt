package io.craigmiller160.expensetrackerapi.data.model.core

import java.time.ZonedDateTime
import javax.persistence.MappedSuperclass
import javax.persistence.PreUpdate
import javax.persistence.Version

@MappedSuperclass
abstract class MutableTableEntity<T> : TableEntity<T>() {
  var updated: ZonedDateTime = ZonedDateTime.now()
  @Version var version: Long = 1

  override fun onPrePersist() {
    super.onPrePersist()
    updated = ZonedDateTime.now()
  }

  @PreUpdate
  open fun onPreUpdate() {
    updated = ZonedDateTime.now()
  }
}
