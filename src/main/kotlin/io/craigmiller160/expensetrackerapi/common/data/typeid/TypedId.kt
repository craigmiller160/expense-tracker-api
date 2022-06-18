package io.craigmiller160.expensetrackerapi.common.data.typeid

import java.io.Serializable
import java.util.UUID

abstract class TypedId(val uuid: UUID) : Serializable {
  override fun toString(): String = uuid.toString()
}
