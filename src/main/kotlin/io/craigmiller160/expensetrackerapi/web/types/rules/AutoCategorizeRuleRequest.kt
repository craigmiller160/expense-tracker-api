package io.craigmiller160.expensetrackerapi.web.types.rules

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.validation.ValidRegex
import jakarta.validation.constraints.Min
import java.math.BigDecimal
import java.time.LocalDate

data class AutoCategorizeRuleRequest(
    val categoryId: TypedId<CategoryId>,
    @field:ValidRegex val regex: String,
    @field:Min(1) val ordinal: Int? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null
)
