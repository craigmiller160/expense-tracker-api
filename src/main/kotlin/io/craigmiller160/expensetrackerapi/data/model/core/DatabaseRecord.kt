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
  @Id var uid: TypedId<T> = TypedId()
  @Transient private var innerIsNew: Boolean = true
  override fun getId(): TypedId<T> = uid

  override fun isNew(): Boolean = innerIsNew

  private fun handleIsNew() {
    innerIsNew = false
  }

  @PrePersist
  open fun onPrePersist() {
    handleIsNew()
  }

  @PostLoad
  open fun onPostLoad() {
    handleIsNew()
  }

  @PreUpdate open fun onPreUpdate() {}

  override fun equals(other: Any?): Boolean {
    if (other !is TableEntity<*>) return false
    return other.uid == this.uid
  }
  override fun hashCode(): Int = this.uid.hashCode()
}
