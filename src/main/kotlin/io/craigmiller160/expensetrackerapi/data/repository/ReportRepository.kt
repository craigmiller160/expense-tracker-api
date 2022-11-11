package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.data.projection.SpendingByMonth
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import org.springframework.data.domain.Page

interface ReportRepository {
  companion object {
    const val UNKNOWN_CATEGORY_NAME = "Unknown"
    const val UNKNOWN_CATEGORY_COLOR = "#3e442a"
  }
  fun getSpendingByMonthAndCategory(userId: Long, request: ReportRequest): Page<SpendingByMonth>
}
