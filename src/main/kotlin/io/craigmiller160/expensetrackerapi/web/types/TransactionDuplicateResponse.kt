package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import java.time.ZonedDateTime

data class TransactionDuplicateResponse(
  val id: TypedId<TransactionId>,
  val created: ZonedDateTime,
  val updated: ZonedDateTime,
  val categoryName: String
)
