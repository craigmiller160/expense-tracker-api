package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime

data class TransactionDetailsResponse(
  val id: TypedId<TransactionId>,
  val expenseDate: LocalDate,
  val description: String,
  val amount: BigDecimal,
  val confirmed: Boolean,
  val duplicate: Boolean,
  val categoryId: TypedId<CategoryId>?,
  val categoryName: String?,
  val created: ZonedDateTime,
  val updated: ZonedDateTime
)
