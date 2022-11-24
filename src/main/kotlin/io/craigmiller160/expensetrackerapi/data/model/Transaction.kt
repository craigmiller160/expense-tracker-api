package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.core.MutableEntity
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transactions")
class Transaction(
  override var userId: Long,
  override var expenseDate: LocalDate,
  override var description: String,
  override var amount: BigDecimal,
  @Column(name = "content_hash", insertable = false, updatable = false)
  var contentHash: String = "",
  var markNotDuplicateNano: Long? = null,
  override var confirmed: Boolean = false,
  override var categoryId: TypedId<CategoryId>? = null,
) : MutableEntity<TransactionId>(), TransactionCommon {
  constructor(
    other: Transaction
  ) : this(
    userId = other.userId,
    expenseDate = other.expenseDate,
    description = other.description,
    amount = other.amount,
    contentHash = other.contentHash,
    markNotDuplicateNano = other.markNotDuplicateNano,
    confirmed = other.confirmed,
    categoryId = other.categoryId)
}
