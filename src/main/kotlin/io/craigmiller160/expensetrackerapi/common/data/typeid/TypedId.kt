package io.craigmiller160.expensetrackerapi.common.data.typeid

import java.io.Serializable
import java.util.UUID

class TypedId(val uuid: UUID = UUID.randomUUID()) : Serializable {
  // TODO don't forget the jackson serializer

  constructor(id: String) : this(UUID.fromString(id))
  override fun toString(): String = uuid.toString()
}
