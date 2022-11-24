package io.craigmiller160.expensetrackerapi.data.model.core

import java.time.ZonedDateTime
import javax.persistence.MappedSuperclass
import javax.persistence.PrePersist

@MappedSuperclass
abstract class TableEntity<T> : DatabaseRecord<T>() {
  var created: ZonedDateTime = ZonedDateTime.now()

  @PrePersist
  open fun onPrePersist() {
    created = ZonedDateTime.now()
  }
}
