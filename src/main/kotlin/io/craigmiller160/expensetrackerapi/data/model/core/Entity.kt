package io.craigmiller160.expensetrackerapi.data.model.core

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import java.time.ZonedDateTime
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.PrePersist
import org.hibernate.annotations.TypeDef

@MappedSuperclass
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
abstract class Entity<T> {
  @Id var id: TypedId<T> = TypedId()
  var created: ZonedDateTime = ZonedDateTime.now()

  @PrePersist
  fun onPrePersist() {
    created = ZonedDateTime.now()
  }
}
