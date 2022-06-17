package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typeid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typeid.ids.TransactionId
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "transactions")
data class Transaction(
    val userId: Long,
    val expenseDate: LocalDate,
    val description: String,
    val amount: BigDecimal,
    val confirmed: Boolean,
    val categoryId: CategoryId? = null,
    @Id val id: TransactionId = TransactionId()
)
