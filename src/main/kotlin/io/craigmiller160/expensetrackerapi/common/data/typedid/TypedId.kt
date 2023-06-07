package io.craigmiller160.expensetrackerapi.common.data.typedid

import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable
import java.util.UUID

@Schema(implementation = UUID::class)
data class TypedId<T>(val uuid: UUID = UUID.randomUUID()) : Serializable, Comparable<TypedId<T>> {
  constructor(id: String) : this(UUID.fromString(id))

  override fun compareTo(other: TypedId<T>): Int = this.uuid.compareTo(other.uuid)
  override fun toString(): String = uuid.toString()
}
