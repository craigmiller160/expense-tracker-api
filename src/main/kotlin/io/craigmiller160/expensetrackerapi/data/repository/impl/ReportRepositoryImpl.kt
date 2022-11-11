package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.data.projection.SpendingByMonth
import io.craigmiller160.expensetrackerapi.data.repository.ReportRepository
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import javax.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.stereotype.Repository

@Repository
class ReportRepositoryImpl : ReportRepository {
  @Transactional
  override fun getSpendingByMonthAndCategory(
    userId: Long,
    request: ReportRequest
  ): Page<SpendingByMonth> {
    TODO("Not yet implemented")
  }
}
