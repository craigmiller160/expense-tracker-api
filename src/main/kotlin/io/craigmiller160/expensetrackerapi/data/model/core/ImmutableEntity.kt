package io.craigmiller160.expensetrackerapi.data.model.core

import javax.persistence.MappedSuperclass
import javax.persistence.PreUpdate

@MappedSuperclass
abstract class ImmutableEntity<T> : Entity<T>() {
  @PreUpdate
  fun onPreUpdate() {
    throw IllegalStateException("Cannot update an immutable entity")
  }
}
