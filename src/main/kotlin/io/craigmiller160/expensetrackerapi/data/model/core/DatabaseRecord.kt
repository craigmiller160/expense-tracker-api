package io.craigmiller160.expensetrackerapi.data.model.core

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.PostLoad
import javax.persistence.PrePersist
import org.hibernate.annotations.TypeDef
import org.springframework.data.domain.Persistable

@MappedSuperclass
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
abstract class DatabaseRecord<T> :
  Persistable<
    TypedId<T>> { // TODO try removing Persistable and then renaming ID column if that still works
  // TODO rename this, then rename the DB columns to match
  @Id @Column(name = "id") var recordId: TypedId<T> = TypedId()
  @Transient var _isNew: Boolean = true

  override fun getId(): TypedId<T> = recordId
  override fun isNew(): Boolean = _isNew

  @PrePersist
  @PostLoad
  fun handleIsPersisted() {
    _isNew = false
  }

  override fun equals(other: Any?): Boolean {
    if (other !is TableEntity<*>) return false
    return other.id == this.id
  }
  override fun hashCode(): Int = this.id.hashCode()
}
