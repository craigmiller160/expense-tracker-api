package io.craigmiller160.expensetrackerapi.web.types.report

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByCategory
import java.math.BigDecimal

data class ReportCategoryResponse(
  val id: TypedId<CategoryId>,
  val name: String,
  val amount: BigDecimal,
  val percent: BigDecimal
) {
  companion object {
    fun from(category: SpendingByCategory, monthTotal: BigDecimal): ReportCategoryResponse =
      ReportCategoryResponse(
        id = TODO(),
        name = category.categoryName,
        amount = category.total,
        percent = category.total / monthTotal)
  }
}
