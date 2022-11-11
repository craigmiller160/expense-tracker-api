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
  @Id override val id: TypedId<TransactionId>,
  override val userId: Long,
  override val expenseDate: LocalDate,
  override val description: String,
  override val amount: BigDecimal,
  val contentHash: String,
  override val categoryId: TypedId<CategoryId>?,
  val categoryName: String?,
  override val confirmed: Boolean,
  val duplicate: Boolean,
  val created: ZonedDateTime,
  val updated: ZonedDateTime
) : TransactionCommon
