package io.craigmiller160.expensetrackerapi.common.data.typedid

import java.io.Serializable
import java.util.UUID

data class TypedId<T>(val uuid: UUID = UUID.randomUUID()) : Serializable {
  constructor(id: String) : this(UUID.fromString(id))
  override fun toString(): String = uuid.toString()
}
