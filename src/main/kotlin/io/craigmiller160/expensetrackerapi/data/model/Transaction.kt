package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.core.MutableEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "transactions")
data class Transaction(
    val userId: Long,
    val expenseDate: LocalDate,
    val description: String,
    val amount: BigDecimal,
    val confirmed: Boolean,
    val categoryId: TypedId<CategoryId>? = null,
    @Id override val id: TypedId<TransactionId> = TypedId(),
    override val created: ZonedDateTime = ZonedDateTime.now(),
    override var updated: ZonedDateTime = ZonedDateTime.now(),
    @Version override val version: Long = 1
) : MutableEntity<TransactionId>
