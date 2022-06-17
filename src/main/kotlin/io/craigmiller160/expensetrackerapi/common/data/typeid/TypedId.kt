package io.craigmiller160.expensetrackerapi.common.data.typeid

import java.util.UUID

abstract class TypedId(val uuid: UUID) {
  override fun toString(): String = uuid.toString()
}
