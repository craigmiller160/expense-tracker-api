package io.craigmiller160.expensetrackerapi.data.model.core

import jakarta.persistence.MappedSuperclass
import java.lang.IllegalStateException

@MappedSuperclass
abstract class ViewEntity<T> : DatabaseRecord<T>() {
  override fun onPrePersist() {
    throw IllegalStateException("Cannot persist a view")
  }

  override fun onPreUpdate() {
    throw IllegalStateException("Cannot update a view")
  }
}
