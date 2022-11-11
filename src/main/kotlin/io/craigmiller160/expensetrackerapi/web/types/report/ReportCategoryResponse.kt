package io.craigmiller160.expensetrackerapi.web.types.report

import io.craigmiller160.expensetrackerapi.data.projection.SpendingByCategory
import java.math.BigDecimal

data class ReportCategoryResponse(
  val name: String,
  val color: String,
  val amount: BigDecimal,
  val percent: BigDecimal
) {
  companion object {
    fun from(category: SpendingByCategory, monthTotal: BigDecimal): ReportCategoryResponse =
      ReportCategoryResponse(
        name = category.categoryName,
        color = category.color,
        amount = category.amount,
        percent = category.amount / monthTotal)
  }
}
