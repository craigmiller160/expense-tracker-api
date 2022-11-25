package io.craigmiller160.expensetrackerapi.data.model.core

import java.time.ZonedDateTime
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class TableEntity<T> : DatabaseRecord<T>() {
  var created: ZonedDateTime = ZonedDateTime.now()

  override fun onPrePersist() {
    super.onPrePersist()
    created = ZonedDateTime.now()
  }
}
