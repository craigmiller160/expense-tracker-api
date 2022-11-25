package io.craigmiller160.expensetrackerapi.data.model.core

import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class ImmutableTableEntity<T> : TableEntity<T>() {
  override fun onPreUpdate() {
    throw IllegalStateException("Cannot update an immutable entity")
  }
}
