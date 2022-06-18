package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import java.time.ZonedDateTime
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.PreUpdate
import javax.persistence.Version
import org.hibernate.annotations.OptimisticLocking
import org.hibernate.annotations.Type
import org.springframework.data.annotation.ReadOnlyProperty

@MappedSuperclass
@OptimisticLocking
abstract class AbstractEntity<T> {
  @get:Id
  @get:Type(type = "io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType")
  abstract val id: TypedId<T>
  abstract val created: ZonedDateTime
  abstract var updated: ZonedDateTime
  @get:Version @get:ReadOnlyProperty abstract val version: Long

  @PreUpdate
  fun onPreUpdate() {
    updated = ZonedDateTime.now()
  }
}
