package io.craigmiller160.expensetrackerapi.web.types.transaction

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import java.math.BigDecimal
import java.time.LocalDate

// TODO needs validation
data class CreateTransactionRequest(
    val expenseDate: LocalDate,
    val description: String,
    val amount: BigDecimal,
    val categoryId: TypedId<CategoryId>?
)
