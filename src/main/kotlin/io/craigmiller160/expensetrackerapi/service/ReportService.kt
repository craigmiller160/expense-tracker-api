package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.data.repository.ReportRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.report.ReportPageResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ReportService(private val reportRepository: ReportRepository) {
  @Transactional
  fun getSpendingByMonthAndCategory(request: ReportRequest): TryEither<ReportPageResponse> {
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //    return Either.catch { reportRepository.getSpendingByMonthAndCategory(userId, request) }
    //      .map { ReportPageResponse.from(it) }
    TODO()
  }
}
