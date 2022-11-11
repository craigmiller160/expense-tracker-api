package io.craigmiller160.expensetrackerapi.web.types.report

import io.craigmiller160.expensetrackerapi.data.projection.SpendingByMonth
import io.craigmiller160.expensetrackerapi.web.types.PageableResponse
import org.springframework.data.domain.Page

data class ReportPageResponse(
  val reports: List<ReportMonthResponse>,
  override val pageNumber: Int,
  override val totalItems: Long
) : PageableResponse {
  companion object {
    fun from(page: Page<SpendingByMonth>): ReportPageResponse =
      ReportPageResponse(
        pageNumber = page.number,
        totalItems = page.totalElements,
        reports = page.content.map { ReportMonthResponse.from(it) })
  }
}
