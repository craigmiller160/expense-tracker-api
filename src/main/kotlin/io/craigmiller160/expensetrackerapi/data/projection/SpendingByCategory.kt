package io.craigmiller160.expensetrackerapi.data.projection

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import java.math.BigDecimal
import java.time.LocalDate

data class SpendingByCategory(
    val categoryId: TypedId<CategoryId>,
    val month: LocalDate,
    val categoryName: String,
    val color: String,
    val amount: BigDecimal
)
