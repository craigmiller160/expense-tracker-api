package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.JpaTypedId
import io.craigmiller160.expensetrackerapi.data.model.core.ViewEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "transactions_view")
class TransactionView(
    @JpaTypedId override var userId: TypedId<UserId>,
    override var expenseDate: LocalDate,
    override var description: String,
    override var amount: BigDecimal,
    var contentHash: String,
    @JpaTypedId override var categoryId: TypedId<CategoryId>?,
    var categoryName: String?,
    override var confirmed: Boolean,
    var duplicate: Boolean,
    var created: ZonedDateTime,
    var updated: ZonedDateTime
) : ViewEntity<TransactionId>(), TransactionCommon
