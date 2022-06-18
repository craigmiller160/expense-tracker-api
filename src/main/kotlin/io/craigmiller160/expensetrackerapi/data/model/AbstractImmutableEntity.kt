package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import java.time.ZonedDateTime
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.PreUpdate
import org.hibernate.annotations.Type

@MappedSuperclass
abstract class AbstractImmutableEntity<T>(
    @Id
    @Type(type = "io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType")
    val id: TypedId<T> = TypedId(),
    val created: ZonedDateTime = ZonedDateTime.now()
) {
  @PreUpdate
  open fun onPreUpdate() {
    throw IllegalStateException("Cannot update an immutable entity")
  }
}
