package io.craigmiller160.expensetrackerapi.data.model.core

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import java.time.ZonedDateTime
import org.hibernate.annotations.TypeDef

@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
interface Entity<T> {
  val id: TypedId<T>
  val created: ZonedDateTime
}
