package io.craigmiller160.expensetrackerapi.data.model.core

import java.lang.IllegalStateException
import javax.persistence.MappedSuperclass
import javax.persistence.PrePersist
import javax.persistence.PreUpdate

@MappedSuperclass
abstract class ViewEntity<T> : DatabaseRecord<T>() {
  @PrePersist
  fun onPrePersist() {
    throw IllegalStateException("Cannot persist a view")
  }

  @PreUpdate
  fun onPreUpdate() {
    throw IllegalStateException("Cannot update a view")
  }
}
