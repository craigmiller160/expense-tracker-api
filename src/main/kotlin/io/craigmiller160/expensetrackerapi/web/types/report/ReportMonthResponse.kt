package io.craigmiller160.expensetrackerapi.web.types.report

import io.craigmiller160.expensetrackerapi.data.projection.SpendingByMonth
import java.math.BigDecimal
import java.time.LocalDate

data class ReportMonthResponse(
    val date: LocalDate,
    val total: BigDecimal,
    val categories: List<ReportCategoryResponse>
) {
  companion object {
    fun from(month: SpendingByMonth): ReportMonthResponse =
        ReportMonthResponse(
            date = month.month,
            total = month.total,
            categories = month.categories.map { ReportCategoryResponse.from(it, month.total) })
  }
}
