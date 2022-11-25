package io.craigmiller160.expensetrackerapi.data.model.core

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import javax.persistence.*
import kotlin.jvm.Transient
import org.hibernate.annotations.TypeDef
import org.springframework.data.domain.Persistable

@MappedSuperclass
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
abstract class DatabaseRecord<T> : Persistable<TypedId<T>> {
  // TODO rename this, then rename the DB columns to match
  @Id @Column(name = "id") var recordId: TypedId<T> = TypedId()
  @Transient private var innerIsNew: Boolean = true
  override fun getId(): TypedId<T> = recordId

  override fun isNew(): Boolean = innerIsNew

  @PrePersist
  @PostLoad
  fun handleIsNew() {
    innerIsNew = false
  }

  override fun equals(other: Any?): Boolean {
    if (other !is TableEntity<*>) return false
    return other.recordId == this.recordId
  }
  override fun hashCode(): Int = this.recordId.hashCode()
}
