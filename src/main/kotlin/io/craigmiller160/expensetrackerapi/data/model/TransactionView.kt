package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "transactions_view")
data class TransactionView(
  @Id val id: TypedId<TransactionId>,
  val userId: Long,
  val expenseDate: LocalDate,
  val description: String,
  val amount: BigDecimal,
  val contentHash: String,
  val categoryId: TypedId<CategoryId>?,
  val categoryName: String?,
  val confirmed: Boolean,
  val duplicate: Boolean,
  val created: ZonedDateTime,
  val updated: ZonedDateTime
)
