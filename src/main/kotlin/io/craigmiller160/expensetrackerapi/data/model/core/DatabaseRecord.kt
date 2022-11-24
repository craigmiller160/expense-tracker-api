package io.craigmiller160.expensetrackerapi.data.model.core

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import org.hibernate.annotations.TypeDef

@MappedSuperclass
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
abstract class DatabaseRecord<T> {
  @Id var id: TypedId<T> = TypedId()

  override fun equals(other: Any?): Boolean {
    if (other !is TableEntity<*>) return false
    return other.id == this.id
  }
  override fun hashCode(): Int = this.id.hashCode()
}
