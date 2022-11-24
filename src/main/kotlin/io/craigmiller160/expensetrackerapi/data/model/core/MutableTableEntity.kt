package io.craigmiller160.expensetrackerapi.data.model.core

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import java.time.ZonedDateTime
import javax.persistence.MappedSuperclass
import javax.persistence.PreUpdate
import javax.persistence.Version
import org.springframework.data.domain.Persistable

@MappedSuperclass
abstract class MutableTableEntity<T> : TableEntity<T>(), Persistable<TypedId<T>> {
  var updated: ZonedDateTime = ZonedDateTime.now()
  @Version var version: Long = 1

  @PreUpdate
  fun onPreUpdate() {
    updated = ZonedDateTime.now()
  }
}
