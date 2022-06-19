package io.craigmiller160.expensetrackerapi.data.model.core

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import java.time.ZonedDateTime
import javax.persistence.PreUpdate
import org.hibernate.annotations.TypeDef

@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
interface MutableEntity<T> {
  val id: TypedId<T>
  val created: ZonedDateTime
  var updated: ZonedDateTime
  val version: Long

  @PreUpdate
  fun onPreUpdate() {
    updated = ZonedDateTime.now()
  }
}
