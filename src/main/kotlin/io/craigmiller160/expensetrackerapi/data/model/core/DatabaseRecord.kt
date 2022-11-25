package io.craigmiller160.expensetrackerapi.data.model.core

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import org.hibernate.annotations.TypeDef

@MappedSuperclass
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
abstract class DatabaseRecord<T> {
  // TODO rename this, then rename the DB columns to match
  @Id @Column(name = "id") var recordId: TypedId<T> = TypedId()

  override fun equals(other: Any?): Boolean {
    if (other !is TableEntity<*>) return false
    return other.recordId == this.recordId
  }
  override fun hashCode(): Int = this.recordId.hashCode()
}
