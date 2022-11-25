package io.craigmiller160.expensetrackerapi.data.model.core

import java.lang.IllegalStateException
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class ViewEntity<T> : DatabaseRecord<T>() {
  override fun onPrePersist() {
    throw IllegalStateException("Cannot persist a view")
  }

  override fun onPreUpdate() {
    throw IllegalStateException("Cannot update a view")
  }
}
