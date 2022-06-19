package io.craigmiller160.expensetrackerapi.data.model.core

import javax.persistence.PreUpdate

interface ImmutableEntity<T> : Entity<T> {
  @PreUpdate
  fun onPreUpdate() {
    throw IllegalStateException("Cannot update an immutable entity")
  }
}
