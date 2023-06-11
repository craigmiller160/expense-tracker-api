package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.JpaTypedId
import io.craigmiller160.expensetrackerapi.data.model.core.MutableTableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "transactions")
class Transaction(
    @JpaTypedId override var userId: TypedId<UserId>,
    override var expenseDate: LocalDate,
    override var description: String,
    override var amount: BigDecimal,
    @Column(name = "content_hash", insertable = false, updatable = false)
    var contentHash: String = "",
    var markNotDuplicateNano: Long? = null,
    override var confirmed: Boolean = false,
    @JpaTypedId override var categoryId: TypedId<CategoryId>? = null,
) : MutableTableEntity<TransactionId>(), TransactionCommon {
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
