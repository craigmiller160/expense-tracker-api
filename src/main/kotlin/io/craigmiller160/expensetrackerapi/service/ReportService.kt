package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import io.craigmiller160.expensetrackerapi.data.repository.ReportRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.report.ReportPageResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val authorizationService: AuthorizationService
) {
  @Transactional
  fun getSpendingByMonthAndCategory(request: ReportRequest): TryEither<ReportPageResponse> {
    val userId = authorizationService.getAuthUserId()
    return Either.catch { reportRepository.getSpendingByMonthAndCategory(userId, request) }
        .map { ReportPageResponse.from(it) }
  }
}
