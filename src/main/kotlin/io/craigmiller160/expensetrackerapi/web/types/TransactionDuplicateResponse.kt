package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.TransactionView
import java.time.ZonedDateTime

data class TransactionDuplicateResponse(
  val id: TypedId<TransactionId>,
  val created: ZonedDateTime,
  val updated: ZonedDateTime,
  val categoryName: String?
) {
  companion object {
    fun from(transaction: TransactionView): TransactionDuplicateResponse =
      TransactionDuplicateResponse(
        id = transaction.id,
        created = transaction.created,
        updated = transaction.updated,
        categoryName = transaction.categoryName)
  }
}
