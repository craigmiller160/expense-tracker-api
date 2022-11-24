package io.craigmiller160.expensetrackerapi.data.model.core

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import java.time.ZonedDateTime

// TODO delete this
interface LegacyEntity<T> {
  val id: TypedId<T>
  val created: ZonedDateTime
}
