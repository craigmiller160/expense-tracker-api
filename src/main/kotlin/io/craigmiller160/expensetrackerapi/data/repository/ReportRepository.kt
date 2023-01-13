package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByMonth
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import org.springframework.data.domain.Page

interface ReportRepository {
  fun getSpendingByMonthAndCategory(
    userId: TypedId<UserId>,
    request: ReportRequest
  ): Page<SpendingByMonth>
}
