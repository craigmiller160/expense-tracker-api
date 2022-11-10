package io.craigmiller160.expensetrackerapi.web.types.report

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import java.math.BigDecimal

data class ReportCategoryResponse(
  val id: TypedId<CategoryId>,
  val name: String,
  val amount: BigDecimal,
  val percent: BigDecimal
)
