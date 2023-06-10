package io.craigmiller160.expensetrackerapi.data.model.core

import jakarta.persistence.MappedSuperclass
import java.time.ZonedDateTime

@MappedSuperclass
abstract class TableEntity<T> : DatabaseRecord<T>() {
  var created: ZonedDateTime = ZonedDateTime.now()

  override fun onPrePersist() {
    super.onPrePersist()
    created = ZonedDateTime.now()
  }
}
