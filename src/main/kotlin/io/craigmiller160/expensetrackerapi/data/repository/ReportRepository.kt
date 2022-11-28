package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.data.projection.SpendingByMonth
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import org.springframework.data.domain.Page

interface ReportRepository {
  fun getSpendingByMonthAndCategory(userId: Long, request: ReportRequest): Page<SpendingByMonth>
}
