package io.craigmiller160.expensetrackerapi.common.data.typedid

import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable
import java.util.UUID

@Schema(implementation = UUID::class)
data class TypedId<T>(val uuid: UUID = UUID.randomUUID()) : Serializable {
  constructor(id: String) : this(UUID.fromString(id))
  override fun toString(): String = uuid.toString()
}
