package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.data.repository.ReportRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.report.ReportPageResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import io.craigmiller160.oauth2.service.OAuth2Service
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ReportService(
  private val reportRepository: ReportRepository,
  private val oAuth2Service: OAuth2Service
) {
  @Transactional
  fun getSpendingByMonthAndCategory(request: ReportRequest): TryEither<ReportPageResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    TODO()
  }
}
