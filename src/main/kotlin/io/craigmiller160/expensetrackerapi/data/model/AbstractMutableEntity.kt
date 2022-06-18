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
abstract class AbstractMutableEntity<T>(
    @Id
    @Type(type = "io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType")
    val id: TypedId<T> = TypedId(),
    val created: ZonedDateTime = ZonedDateTime.now(),
    var updated: ZonedDateTime = ZonedDateTime.now(),
    @Version @ReadOnlyProperty val version: Long = 1
) {
  @PreUpdate
  fun onPreUpdate() {
    updated = ZonedDateTime.now()
  }
}
